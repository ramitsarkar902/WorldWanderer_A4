package flight;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * JUnit 5 tests for FlightSearch.runFlightSearch
 * Each test targets a specific condition (1..11) plus an "all valid" suite.
 */
public class FlightSearchTest {

    private Clock fixedClock;
    private FlightSearch fs;

    // Fix "today" to 13 Oct 2025 in Australia/Melbourne to align with assignment timeline
    @BeforeEach
    void setup() {
        ZoneId zone = ZoneId.of("Australia/Melbourne");
        this.fixedClock = Clock.fixed(Instant.parse("2025-10-12T14:00:00Z"), zone); // 13/10/2025 local
        this.fs = new FlightSearch(fixedClock);
    }

    private boolean call(String depDate, String depAirport, boolean emergency,
                         String retDate, String destAirport, String seatClass,
                         int adults, int children, int infants) {
        return fs.runFlightSearch(depDate, depAirport, emergency, retDate, destAirport, seatClass, adults, children, infants);
    }

    // ---------- Condition 1: total passengers 1..9 ----------
    @ParameterizedTest
    @DisplayName("Condition 1: total passengers boundary")
    @CsvSource({
        // dep,depAC,emg,ret,destAC,class,ad,ch,in,expected
        "14/10/2025,mel,false,20/10/2025,pvg,economy,0,0,0,false",  // 0 total -> invalid
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,true",   // 1 -> valid
        "14/10/2025,mel,false,20/10/2025,pvg,economy,5,2,2,true",   // 9 -> valid
        "14/10/2025,mel,false,20/10/2025,pvg,economy,9,1,0,false"   // 10 -> invalid
    })
    void cond1_totalPassengers(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                               int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 2: children not in emergency/first ----------
    @ParameterizedTest
    @DisplayName("Condition 2: children + emergency/first are invalid")
    @CsvSource({
        "14/10/2025,mel,true,20/10/2025,pvg,economy,1,1,0,false",   // emergency rows + child -> invalid
        "14/10/2025,mel,false,20/10/2025,pvg,first,1,1,0,false"     // first class + child -> invalid
    })
    void cond2_childrenRestrictions(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                                    int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 3: infants not in emergency/business ----------
    @ParameterizedTest
    @DisplayName("Condition 3: infants + emergency/business are invalid")
    @CsvSource({
        "14/10/2025,mel,true,20/10/2025,pvg,economy,1,0,1,false",    // emergency rows + infant -> invalid
        "14/10/2025,mel,false,20/10/2025,pvg,business,1,0,1,false"   // business class + infant -> invalid
    })
    void cond3_infantRestrictions(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                                  int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 4: <=2 children per adult ----------
    @ParameterizedTest
    @DisplayName("Condition 4: children must be <= 2 per adult")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,3,0,false",   // 3 children, 1 adult -> invalid
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,2,0,true"     // 2 children, 1 adult -> valid
    })
    void cond4_childrenPerAdult(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                                int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 5: <=1 infant per adult ----------
    @ParameterizedTest
    @DisplayName("Condition 5: infants <= adults")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,0,2,false",   // 2 infants, 1 adult -> invalid
        "14/10/2025,mel,false,20/10/2025,pvg,economy,2,0,2,true"     // 2 infants, 2 adults -> valid
    })
    void cond5_infantsPerAdult(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                               int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 6: departure not in the past ----------
    @ParameterizedTest
    @DisplayName("Condition 6: departure date cannot be in the past")
    @CsvSource({
        "12/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,false",  // past (yesterday) relative to fixed clock
        "13/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,true"    // today -> valid
    })
    void cond6_departureNotPast(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                                int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 7: strict date format & validity ----------
    @ParameterizedTest
    @DisplayName("Condition 7: strict date parsing/validation")
    @CsvSource({
        "29/02/2026,mel,false,05/03/2026,pvg,economy,1,0,0,false",   // 2026 not leap year
        "29/02/2024,mel,false,05/03/2024,pvg,economy,1,0,0,true"     // 2024 leap year
    })
    void cond7_strictDates(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                           int ad, int ch, int in, boolean expected) {
        // Override fixed clock near the test dates to avoid past/future failures
        ZoneId zone = ZoneId.of("Australia/Melbourne");
        Clock c = Clock.fixed(LocalDate.of(2024, 2, 1).atStartOfDay(zone).toInstant(), zone);
        FlightSearch localFs = new FlightSearch(c);
        assertEquals(expected, localFs.runFlightSearch(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 8: return date not before departure ----------
    @ParameterizedTest
    @DisplayName("Condition 8: return date must be on/after departure")
    @CsvSource({
        "14/10/2025,mel,false,13/10/2025,pvg,economy,1,0,0,false",   // return before dep -> invalid
        "14/10/2025,mel,false,14/10/2025,pvg,economy,1,0,0,true"     // same day -> valid
    })
    void cond8_returnAfterDeparture(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                                    int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 9: seating class allowed ----------
    @ParameterizedTest
    @DisplayName("Condition 9: seating class must be valid")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,pvg,ultra,1,0,0,false",      // invalid class
        "14/10/2025,mel,false,20/10/2025,pvg,premium economy,1,0,0,true" // valid class
    })
    void cond9_classAllowed(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                            int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 10: emergency rows only in economy ----------
    @ParameterizedTest
    @DisplayName("Condition 10: emergency rows only for economy class")
    @CsvSource({
        "14/10/2025,mel,true,20/10/2025,pvg,business,1,0,0,false",    // emergency + non-economy -> invalid
        "14/10/2025,mel,true,20/10/2025,pvg,economy,1,0,0,true"       // emergency + economy -> valid
    })
    void cond10_emergencyOnlyEconomy(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                                     int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    // ---------- Condition 11: airport codes and distinct ----------
    @ParameterizedTest
    @DisplayName("Condition 11: airports must be allowed and different")
    @CsvSource({
        "14/10/2025,mel,false,20/10/2025,mel,economy,1,0,0,false",    // same airport -> invalid
        "14/10/2025,abc,false,20/10/2025,pvg,economy,1,0,0,false",    // invalid code -> invalid
        "14/10/2025,mel,false,20/10/2025,pvg,economy,1,0,0,true"      // valid pair -> valid
    })
    void cond11_airportCodes(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                             int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    @ParameterizedTest
    @DisplayName("All-valid scenarios")
    @CsvSource({
        // economy with emergency rows, 2 adults + 2 children (<=2 per adult), no infants
        "14/10/2025,mel,true,20/10/2025,pvg,economy,2,2,0,true",
        // premium economy, no emergency rows, adult-only
        "14/10/2025,mel,false,16/10/2025,pvg,premium economy,1,0,0,true",
        // business, no infants, with children=0
        "14/10/2025,mel,false,18/10/2025,pvg,business,2,0,0,true",
        // first, no children, one infant not allowed in business but allowed here? -> infants allowed in first (not restricted), ensure no emergency
        "14/10/2025,mel,false,18/10/2025,pvg,first,1,0,0,true"
    })
    void allValid(String dep, String depAC, boolean emg, String ret, String destAC, String cls,
                  int ad, int ch, int in, boolean expected) {
        assertEquals(expected, call(dep, depAC, emg, ret, destAC, cls, ad, ch, in));
    }

    @Test
    @DisplayName("Invalid call must not mutate previously stored valid attributes")
    void noMutationOnInvalid() {
        boolean ok1 = call("14/10/2025","mel",false,"20/10/2025","pvg","economy",1,0,0);
        assertTrue(ok1);
        assertEquals("14/10/2025", fs.getDepartureDate());
        boolean ok2 = call("12/10/2025","mel",false,"20/10/2025","pvg","economy",0,0,0);
        assertFalse(ok2);
        assertEquals("14/10/2025", fs.getDepartureDate());
    }
}
