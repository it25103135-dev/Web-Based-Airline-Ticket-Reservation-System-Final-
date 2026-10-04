package com.lankawings.util;

import com.lankawings.dao.FlightDAO;
import org.junit.Test;
import java.sql.Timestamp;
import static org.junit.Assert.*;

public class FlightScheduleRulesTest {
    private Timestamp t(String v) { return Timestamp.valueOf(v); }

    @Test public void overlappingFlightsConflict() {
        assertTrue(FlightDAO.overlaps(t("2026-10-10 10:00:00"), t("2026-10-10 12:00:00"),
                t("2026-10-10 11:00:00"), t("2026-10-10 13:00:00")));
    }

    @Test public void touchingFlightsDoNotOverlap() {
        assertFalse(FlightDAO.overlaps(t("2026-10-10 10:00:00"), t("2026-10-10 12:00:00"),
                t("2026-10-10 12:00:00"), t("2026-10-10 14:00:00")));
    }
}
