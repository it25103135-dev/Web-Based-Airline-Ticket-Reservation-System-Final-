package com.lankawings.servlet;

import com.lankawings.dao.PaymentDAO;
import com.lankawings.util.Req;
import com.lankawings.util.ValidationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/** Own payment history; administrators can view and delete everyone's records. */
@WebServlet("/payments")
public class PaymentHistoryServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        if (!Req.isAdmin(r)) { s.sendError(403); return; }
        try {
            if (!"delete".equals(Req.param(r, "action")))
                throw new ValidationException("Unknown payment action.");
            int id = Req.id(r, "paymentId", "payment");
            new PaymentDAO().delete(id, true);
            Req.flash(r, "Payment record deleted. The booking and ticket are unchanged. No refund was issued.");
            s.sendRedirect(r.getContextPath() + "/payments");
        } catch (Exception e) { Req.error(r, e); doGet(r, s); }
    }

    @Override
    protected void doGet(HttpServletRequest r, HttpServletResponse s) throws ServletException, IOException {
        try { r.setAttribute("payments", new PaymentDAO().history(Req.uid(r), Req.isAdmin(r))); } catch (Exception e) { Req.error(r, e); }
        r.getRequestDispatcher("/WEB-INF/views/payments.jsp").forward(r, s);
    }
}
