package flight;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * JUnit 5 tests for FlightSearch.runFlightSearch()
 *  - Added pre-/post-condition validation of class attributes
 */
public class FlightSearchTest {

    private Clock fixedClock;
    private FlightSearch fs;

    @BeforeEach
    void setup() {
        ZoneId zone = ZoneId.of("Australia/Melbourne");
        this.fixedClock = Clock.fixed(Instant.parse("2025-10-12T14:00:00Z"), zone); 
        this.fs = new FlightSearch(fixedClock);
    }

    private boolean call(String depDate, String depAirport, boolean emergency,
                         String retDate, String destAirport, String seatClass,
                         int adults, int children, int infants) {
        return fs.runFlightSearch(depDate, depAirport, emergency,
                                  retDate, destAirport, seatClass,
                                  adults, children, infants);
    }

    // ---------- Condition 1: total passengers + negatives ----------
    @ParameterizedTest
    @DisplayName("Condition 1: total passengers boundary + negative counts")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,pvg,economy,0,0,0,false",   // total = 0
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,true",    // total = 1
        "14/10/2025,mel,false,20/10/2025,pvg,economy,5,2,2,true",    // total = 9
        "14/10/2025,mel,false,20/10/2025,pvg,economy,9,1,0,false",   // >9 invalid
        "14/10/2025,mel,false,20/10/2025,pvg,economy,2,-1,0,false",  // negative children
        "14/10/2025,mel,false,20/10/2025,pvg,economy,2,0,-1,false"   // negative infants
    })
    void cond1_totalPassengers(String dep, String depAC, boolean emg,
                               String ret, String destAC, String cls,
                               int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 2: children not in emergency/first ----------
    @ParameterizedTest
    @DisplayName("Condition 2: children + emergency/first invalid")
    @CsvSource({
        "14/10/2025,mel,true,20/10/2025,pvg,economy,1,1,0,false",
        "14/10/2025,mel,false,20/10/2025,pvg,first,1,1,0,false"
    })
    void cond2_childrenRestrictions(String dep, String depAC, boolean emg,
                                    String ret, String destAC, String cls,
                                    int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 3: infants not in emergency/business ----------
    @ParameterizedTest
    @DisplayName("Condition 3: infants + emergency/business invalid")
    @CsvSource({
        "14/10/2025,mel,true,20/10/2025,pvg,economy,1,0,1,false",
        "14/10/2025,mel,false,20/10/2025,pvg,business,1,0,1,false"
    })
    void cond3_infantRestrictions(String dep, String depAC, boolean emg,
                                  String ret, String destAC, String cls,
                                  int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 4 ----------
    @ParameterizedTest
    @DisplayName("Condition 4: ≤2 children per adult")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,3,0,false",
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,2,0,true"
    })
    void cond4_childrenPerAdult(String dep, String depAC, boolean emg,
                                String ret, String destAC, String cls,
                                int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 5 ----------
    @ParameterizedTest
    @DisplayName("Condition 5: ≤1 infant per adult")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,0,2,false",
        "14/10/2025,mel,false,20/10/2025,pvg,economy,2,0,2,true"
    })
    void cond5_infantsPerAdult(String dep, String depAC, boolean emg,
                               String ret, String destAC, String cls,
                               int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 6 ----------
    @ParameterizedTest
    @DisplayName("Condition 6: departure cannot be past")
    @CsvSource({
        "12/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,false",
        "13/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,true"
    })
    void cond6_departureNotPast(String dep, String depAC, boolean emg,
                                String ret, String destAC, String cls,
                                int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 7 ----------
    @ParameterizedTest
    @DisplayName("Condition 7: strict date parsing")
    @CsvSource({
        "29/02/2026,mel,false,05/03/2026,pvg,economy,1,0,0,false",
        "29/02/2024,mel,false,05/03/2024,pvg,economy,1,0,0,true"
    })
    void cond7_strictDates(String dep, String depAC, boolean emg,
                           String ret, String destAC, String cls,
                           int ad, int ch, int in, boolean expected) {
        ZoneId zone = ZoneId.of("Australia/Melbourne");
        Clock c = Clock.fixed(LocalDate.of(2024, 2, 1).atStartOfDay(zone).toInstant(), zone);
        FlightSearch localFs = new FlightSearch(c);
        assertEquals(expected, localFs.runFlightSearch(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 8 ----------
    @ParameterizedTest
    @DisplayName("Condition 8: return ≥ departure")
    @CsvSource({
        "14/10/2025,mel,false,13/10/2025,pvg,economy,1,0,0,false",
        "14/10/2025,mel,false,14/10/2025,pvg,economy,1,0,0,true"
    })
    void cond8_returnAfterDeparture(String dep, String depAC, boolean emg,
                                    String ret, String destAC, String cls,
                                    int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 9 ----------
    @ParameterizedTest
    @DisplayName("Condition 9: class must be allowed")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,pvg,ultra,1,0,0,false",
        "14/10/2025,mel,false,20/10/2025,pvg,premium economy,1,0,0,true"
    })
    void cond9_classAllowed(String dep, String depAC, boolean emg,
                            String ret, String destAC, String cls,
                            int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 10 ----------
    @ParameterizedTest
    @DisplayName("Condition 10: only economy can have emergency row; all classes can be non-emergency")
    @CsvSource({
        "14/10/2025,mel,true,20/10/2025,pvg,business,1,0,0,false",   // non-economy + emergency → invalid
        "14/10/2025,mel,true,20/10/2025,pvg,economy,1,0,0,true",     // economy + emergency → valid
        "14/10/2025,mel,false,20/10/2025,pvg,business,1,0,0,true",   // any class non-emergency → valid
        "14/10/2025,mel,false,20/10/2025,pvg,first,1,0,0,true"       // first non-emergency → valid
    })

    void cond10_emergencyOnlyEconomy(String dep, String depAC, boolean emg,
                                     String ret, String destAC, String cls,
                                     int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 11 ----------
    @ParameterizedTest
    @DisplayName("Condition 11: airports allowed & distinct")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,mel,economy,1,0,0,false",
        "14/10/2025,abc,false,20/10/2025,pvg,economy,1,0,0,false",
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,true"
    })
    void cond11_airportCodes(String dep, String depAC, boolean emg,
                             String ret, String destAC, String cls,
                             int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    @ParameterizedTest
    @DisplayName("All-valid scenarios")
    @CsvSource({
        "14/10/2025,mel,true,20/10/2025,pvg,economy,2,2,0,false",
        "14/10/2025,mel,false,16/10/2025,pvg,premium economy,1,0,0,true",
        "14/10/2025,mel,false,18/10/2025,pvg,business,2,0,0,true",
        "14/10/2025,mel,false,18/10/2025,pvg,first,1,0,0,true"
    })
    void allValid(String dep, String depAC, boolean emg,
                  String ret, String destAC, String cls,
                  int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    @Test
    @DisplayName("Pre/Post validation: class values update only when valid")
    void attributeUpdateProof() {
        boolean ok1 = call("14/10/2025","mel",false,"20/10/2025","pvg","economy",1,0,0);
        assertTrue(ok1);
        assertEquals("14/10/2025", fs.getDepartureDate());
        assertEquals(1, fs.getAdultPassengerCount());

        // invalid call (negative children)
        boolean ok2 = call("14/10/2025","mel",false,"20/10/2025","pvg","economy",1,-1,0);
        assertFalse(ok2);
        assertEquals("14/10/2025", fs.getDepartureDate());
        assertEquals(1, fs.getAdultPassengerCount());

        // valid update
        boolean ok3 = call("15/10/2025","mel",false,"21/10/2025","pvg","economy",2,0,0);
        assertTrue(ok3);
        assertEquals("15/10/2025", fs.getDepartureDate());
        assertEquals(2, fs.getAdultPassengerCount());
    }
}
