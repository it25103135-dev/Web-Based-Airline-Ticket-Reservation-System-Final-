package com.lankawings.servlet;

import com.lankawings.dao.FeedbackDAO;
import com.lankawings.dao.NotificationDAO;
import com.lankawings.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Feedback & reviews.
 *  Passengers: add, edit (until answered), delete their own.
 *  Admins: respond, close, delete any.
 */
@WebServlet("/feedback")
public class FeedbackServlet extends HttpServlet {
    private final FeedbackDAO dao = new FeedbackDAO();

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try {
            r.setAttribute("feedbackList", dao.list(Req.isAdmin(r) ? null : Req.uid(r)));
            r.setAttribute("stats", dao.stats());
        } catch (Exception e) {
            Req.error(r, e);
        }
        r.getRequestDispatcher("/WEB-INF/views/feedback.jsp").forward(r, s);
    }

    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        boolean admin = Req.isAdmin(r);
        int uid = Req.uid(r);
        try {
            switch (Req.param(r, "action")) {
                case "add" -> {
                    if (admin) throw new ValidationException("Administrators respond to feedback; they don't post reviews.");
                    int rating = Validator.intRange(Req.param(r, "rating"), "Rating", 1, 5);
                    String subject = Validator.text(Req.param(r, "subject"), "Subject", 3, 120);
                    String message = Validator.text(Req.param(r, "message"), "Message", 10, 1000);
                    dao.add(uid, rating, subject, message);
                    try { new NotificationDAO().notifyAdmins("New feedback received", "A passenger submitted " + rating + "-star feedback: " + subject, "FEEDBACK"); }
                    catch (Exception e) { Req.log().log(Level.WARNING, "notify admins failed", e); }
                    Req.flash(r, "Thank you! Your feedback was submitted.");
                }
                case "update" -> {
                    int id = Req.id(r, "id", "feedback");
                    dao.update(id, uid, Validator.intRange(Req.param(r, "rating"), "Rating", 1, 5),
                            Validator.text(Req.param(r, "subject"), "Subject", 3, 120), Validator.text(Req.param(r, "message"), "Message", 10, 1000));
                    Req.flash(r, "Feedback updated.");
                }
                case "delete" -> {
                    dao.delete(Req.id(r, "id", "feedback"), uid, admin);
                    Req.flash(r, "Feedback deleted.");
                }
                case "respond" -> {
                    requireAdmin(admin);
                    int id = Req.id(r, "id", "feedback");
                    String response = Validator.text(Req.param(r, "response"), "Response", 5, 1000);
                    int author = dao.respond(id, response);
                    try { if (author > 0) new NotificationDAO().add(author, "We replied to your feedback", "Lanka Wings has responded to your feedback. Open Feedback to read it.", "FEEDBACK"); }
                    catch (Exception e) { Req.log().log(Level.WARNING, "notify author failed", e); }
                    Req.flash(r, "Response sent.");
                }
                case "close" -> {
                    requireAdmin(admin);
                    dao.close(Req.id(r, "id", "feedback"));
                    Req.flash(r, "Feedback closed.");
                }
                default -> throw new ValidationException("Unknown action.");
            }
            s.sendRedirect(r.getContextPath() + "/feedback");
        } catch (Exception e) {
            Req.error(r, e);
            doGet(r, s);
        }
    }

    private static void requireAdmin(boolean admin) {
        if (!admin) throw new ValidationException("Only administrators can do that.");
    }
}
