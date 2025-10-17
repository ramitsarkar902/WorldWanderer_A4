package flight;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FlightSearch {
   private String  departureDate;
   private String  departureAirportCode;
   private boolean emergencyRowSeating;
   private String  returnDate;
   private String  destinationAirportCode; 
   private String  seatingClass;
   private int     adultPassengerCount;
   private int     childPassengerCount;
   private int     infantPassengerCount;

   private static final Set<String> ALLOWED_CLASSES =
           new HashSet<>(Arrays.asList("economy", "premium economy", "business", "first"));
   private static final Set<String> ALLOWED_AIRPORTS =
           new HashSet<>(Arrays.asList("syd","mel","lax","cdg","del","pvg","doh"));

   private static final DateTimeFormatter DMY =
           DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

   private final Clock clock;

   public FlightSearch() { this.clock = Clock.systemDefaultZone(); }
   public FlightSearch(Clock clock) { this.clock = clock; }

   /**
    * Validates all inputs against the 11 conditions.
    * If valid, initializes attributes and returns true.
    * If invalid, returns false and leaves existing attributes unchanged.
    */
   public boolean runFlightSearch(String departureDate,    String departureAirportCode,   boolean emergencyRowSeating, 
                                  String returnDate,       String destinationAirportCode, String seatingClass, 
                                  int adultPassengerCount, int childPassengerCount,       int infantPassengerCount) {
      // Condition 7: strict date format + valid calendar dates
      LocalDate dep = parseStrictDMY(departureDate);
      LocalDate ret = parseStrictDMY(returnDate);
      if (dep == null || ret == null) return false;

      // Condition 6: departure not in the past
      LocalDate today = LocalDate.now(clock);
      if (dep.isBefore(today)) return false;

      // Condition 8: return date not before departure
      if (ret.isBefore(dep)) return false;

      // Condition 11: airports allowed and distinct
      if (!ALLOWED_AIRPORTS.contains(departureAirportCode) ||
          !ALLOWED_AIRPORTS.contains(destinationAirportCode) ||
          departureAirportCode.equals(destinationAirportCode)) return false;

      // Condition 9: seating class allowed
      if (!ALLOWED_CLASSES.contains(seatingClass)) return false;

      // Condition 1: total passengers 1..9 and no negative children/infants
      if (childPassengerCount < 0 || infantPassengerCount < 0) return false;
      int total = adultPassengerCount + childPassengerCount + infantPassengerCount;
      if (total < 1 || total > 9) return false;

      // Condition 4: up to 2 children per adult if any children present
      if (childPassengerCount > 0) {
          if (adultPassengerCount < 1) return false;
          if (childPassengerCount > 2 * adultPassengerCount) return false;
      }

      // Condition 5: at most 1 infant per adult if any infant present
      if (infantPassengerCount > 0) {
          if (adultPassengerCount < 1) return false;
          if (infantPassengerCount > adultPassengerCount) return false;
      }

      // Condition 2: children cannot be seated in emergency rows or first class
      if (childPassengerCount > 0) {
          if (emergencyRowSeating) return false;
          if ("first".equals(seatingClass)) return false;
      }

      // Condition 3: infants cannot be in emergency rows or business class
      if (infantPassengerCount > 0) {
          if (emergencyRowSeating) return false;
          if ("business".equals(seatingClass)) return false;
      }

      // Condition 10: only economy can have emergency row seating
      if (emergencyRowSeating && !"economy".equals(seatingClass)) return false;

      // All checks passed → update attributes
      this.departureDate = departureDate;
      this.departureAirportCode = departureAirportCode;
      this.emergencyRowSeating = emergencyRowSeating;
      this.returnDate = returnDate;
      this.destinationAirportCode = destinationAirportCode;
      this.seatingClass = seatingClass;
      this.adultPassengerCount = adultPassengerCount;
      this.childPassengerCount = childPassengerCount;
      this.infantPassengerCount = infantPassengerCount;

      return true;
   }

   private LocalDate parseStrictDMY(String dmy) {
       try { return LocalDate.parse(dmy, DMY); }
       catch (Exception e) { return null; }
   }

   // Getters for test validation
   public String getDepartureDate() { return departureDate; }
   public String getDepartureAirportCode() { return departureAirportCode; }
   public boolean isEmergencyRowSeating() { return emergencyRowSeating; }
   public String getReturnDate() { return returnDate; }
   public String getDestinationAirportCode() { return destinationAirportCode; }
   public String getSeatingClass() { return seatingClass; }
   public int getAdultPassengerCount() { return adultPassengerCount; }
   public int getChildPassengerCount() { return childPassengerCount; }
   public int getInfantPassengerCount() { return infantPassengerCount; }
}
