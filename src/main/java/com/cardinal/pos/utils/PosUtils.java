package com.cardinal.pos.utils;

import com.cardinal.pos.model.RentalAgreement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;


public class PosUtils {

    /**
     * calculate current date + # of rental days + any additional holiday days requirements: Independence Day, July 4th
     * - If falls on weekend, it is observed on the closest weekday (if Sat, then Friday before, if Sunday, then Monday
     * after) Labor Day - First Monday in September
     * <p>
     * Interpretation: Independence Day, July 4th - If current date + # of rental days falls on  july 4th of the current
     * year and that is a sunday, then add 1 day to dueDate. and, if it falls on saturday, the holiday is observed on
     * friday, hence does not affect due date Labor Day - First Monday in September - If current date + # of rental days
     * falls on First Monday in September of the current year, then add 1 day to dueDate
     *
     * @param rentalAgreement - RentalAgreement Instance
     * @return due date
     */
    public static LocalDate calculateDueDate(RentalAgreement rentalAgreement) {
        // Calculate the initial due date
        LocalDate currentDate = rentalAgreement.getCheckoutDate();
        LocalDate dueDate = currentDate.plusDays(rentalAgreement.getRentalDayCount());

        // Check if the due date falls on July 4th and is a sunday
        if (dueDate.getMonthValue() == 7 && dueDate.getDayOfMonth() == 4 && (dueDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
            dueDate = dueDate.plusDays(1);
        }

        // Check if the due date falls on the first Monday in September
        if (isLaborDay(dueDate)) {
            dueDate = dueDate.plusDays(1);
        }

        return dueDate;
    }

    /**
     * Requirements: Charge days - Count of chargeable days, from day after checkout through and including due date,
     * excluding “no charge” days as specified by the tool type.
     * <p>
     * Interpretation: given a start date (checkout date + 1) and end date within 30 days of each other, determine the
     * following: the total number of weekend days. set it to a variable called weekendDays
     * <p>
     * does the range include July 4th of the same year, if yes, then set a holidayFlag does the range include the first
     * monday in september of the same year, if yes, then set a holidayFlag get base charge days as days between start
     * and end date if not charge
     *
     * @param rentalAgreement - RentalAgreement Instance
     * @return # of charge days
     */
    public static int calculateChargeDays(RentalAgreement rentalAgreement) {

        int chargeDays;

        LocalDate startDate = rentalAgreement.getCheckoutDate();
        startDate = startDate.plusDays(1);
        LocalDate endDate = rentalAgreement.getDueDate();

        // Validate the date range (ensure it is within 30 days)
        if (ChronoUnit.DAYS.between(startDate, endDate) > 30 || ChronoUnit.DAYS.between(startDate, endDate) < 1) {
            throw new IllegalArgumentException("The date range must be within 30 days and greater than 1 day");
        }

        // Calculate the number of weekend days
        long weekendDays = countWeekendDays(startDate, endDate);

        // Check if the range includes July 4th
        boolean includesJuly4th = includesDate(startDate, endDate, Month.JULY, 4);

        // Check if the range includes the first Monday in September
        boolean includesLaborDay = includesFirstMondayInMonth(startDate, endDate, Month.SEPTEMBER);

        //get charge day special flags
        Boolean isHolidayCharged = rentalAgreement.getTool().getHolidayCharge();
        Boolean isWeekendCharged = rentalAgreement.getTool().getWeekendCharge();

        //get base charge days
        chargeDays = (int) ChronoUnit.DAYS.between(startDate, endDate);

        //there can only be 1 holiday within 30 days given the holidays we are focusing on
        if (!isHolidayCharged && (includesJuly4th || includesLaborDay)) {
            chargeDays--;
        }

        //weekendDays will be subtracted from total days if we do not count them
        if (!isWeekendCharged) {
            chargeDays -= (int) weekendDays;
        }

        return chargeDays;

    }

    /**
     * Requirements: Pre-discount charge - Calculated as charge days X daily charge. Resulting total rounded half up to
     * cents.
     * <p>
     * Interpretation: (requirements sufficient)
     *
     * @param rentalAgreement - RentalAgreement Instance
     * @return Pre Discount Charge
     */
    public static BigDecimal calculatePreDiscountCharge(RentalAgreement rentalAgreement) {
        BigDecimal dailyCharge = rentalAgreement.getTool().getDailyRentalCharge();
        BigDecimal chargeDays = BigDecimal.valueOf(rentalAgreement.getChargeDays());
        return chargeDays.multiply(dailyCharge).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Requirements: Discount amount - calculated from discount % and pre-discount charge. Resulting amount rounded half
     * up to cents.
     * <p>
     * Interpretation: (requirements sufficient)
     *
     * @param rentalAgreement - RentalAgreement Instance
     * @return discount amount
     */
    public static BigDecimal calculateDiscountAmount(RentalAgreement rentalAgreement) {
        BigDecimal discountPercent = BigDecimal.valueOf(rentalAgreement.getDiscountPercent());
        BigDecimal preDiscountCharge = rentalAgreement.getPreDiscountCharge();
        return preDiscountCharge.multiply(discountPercent).divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Requirements: Final charge - Calculated as pre-discount charge - discount amount
     * <p>
     * Interpretation: (requirements sufficient)
     *
     * @param rentalAgreement - RentalAgreement Instance
     * @return final charge
     */
    public static BigDecimal calculateFinalCharge(RentalAgreement rentalAgreement) {
        BigDecimal finalCharge = rentalAgreement.getPreDiscountCharge().subtract(rentalAgreement.getDiscountAmount()).setScale(2, RoundingMode.HALF_UP);
        if (rentalAgreement.getDiscountAmount().compareTo(rentalAgreement.getPreDiscountCharge()) > 0) {
            finalCharge = BigDecimal.valueOf(0);
        }
        return finalCharge;
    }

    private static long countWeekendDays(LocalDate startDate, LocalDate endDate) {
        long weekendDays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
        }
        return weekendDays;
    }

    private static boolean includesDate(LocalDate startDate, LocalDate endDate, Month month, int dayOfMonth) {
        LocalDate targetDate = LocalDate.of(startDate.getYear(), month, dayOfMonth);
        return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
    }

    private static boolean includesFirstMondayInMonth(LocalDate startDate, LocalDate endDate, Month month) {
        int monthValue = month.getValue(); // Convert Month enum to int
        LocalDate firstMondayInMonth = startDate.withMonth(monthValue).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        return !firstMondayInMonth.isBefore(startDate) && !firstMondayInMonth.isAfter(endDate);
    }

    private static boolean isLaborDay(LocalDate dueDate) {
        int year = dueDate.getYear();
        int month = 9; // September

        LocalDate firstMonday = LocalDate.of(year, month, 1);

        while (firstMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
            firstMonday = firstMonday.plusDays(1);
        }
        return firstMonday.isEqual(dueDate);
    }

    public static LocalDate convertToDate(String dt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return LocalDate.parse(dt, formatter);
    }

    public static void checkParmValidity(String rentalDayCount, String discountPercent) throws Exception {
        int rdc = Integer.parseInt(rentalDayCount);
        int dp = Integer.parseInt(discountPercent);
        if (rdc <= 1) {
            throw new Exception("Error: Please enter a valid rental day count - must be >= 1");
        }
        if (!(dp >= 0 && dp <= 100)) {
            throw new Exception("Error: Please enter a discount percent in the range: 0 <= x <= 100");
        }
    }
}
