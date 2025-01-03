package com.cardinal.pos;

import com.cardinal.pos.model.RentalAgreement;
import com.cardinal.pos.model.Tool;
import com.cardinal.pos.repository.RentalAgreementRepository;
import com.cardinal.pos.repository.ToolRepository;
import com.cardinal.pos.utils.PosUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PosController {

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private RentalAgreementRepository rentalAgreementRepository;

    /**
     * given the following:
     * <p>
     * { "toolCode": "ABC123", "rentalDayCount":"5", "discountPercent": "10", "checkoutDate": "2023-03-01T12:00:00" }
     * <p>
     * create a rental agreement following these rules:
     * <p>
     * Tool code - Provided at checkout Discount percent - Provided at checkout. Rental days - Provided at checkout.
     * Number of days requested Check out date - Provided at checkout. Current date Tool type - From tool info - can be
     * retrieved from Tool instance Tool brand - From tool info - can be retrieved from Tool instance Daily rental
     * charge - Amount per day, specified by the tool type. - can be retrieved from Tool instance Due date - CALCULATED
     * from checkout date and rental days. Charge days - CALCULATED from count of chargeable days, from day after
     * checkout through and including due date, excluding “no charge” days as specified by the tool type. Pre-discount
     * charge - CALCULATED as charge days X daily charge. Resulting total rounded half up to cents. Discount amount -
     * CALCULATED from discount % and pre-discount charge. Resulting amount rounded half up to cents. Final charge -
     * CALCULATED as pre-discount charge - discount amount.     *
     */
    @PostMapping("/checkout")
    public ResponseEntity<RentalAgreement> checkout(@RequestBody Map<String, String> rentalParms) {

        RentalAgreement rentalAgreement = new RentalAgreement();
        HttpStatus returnCode;
        try {
            //get body parameters
            String rentalDayCount = rentalParms.get("rentalDayCount");
            String discountPercent = rentalParms.get("discountPercent");
            PosUtils.checkParmValidity(rentalDayCount, discountPercent);
            String toolCode = rentalParms.get("toolCode");
            String checkoutDate = rentalParms.get("checkoutDate");

            //get the Tool from the tool table via unique identifier
            //This would probably be a wrapped query response or a JPA response,
            //but for this impl, we are just using a stream
            Optional<Tool> toolOptional = toolRepository.findByToolCode(toolCode);
            if (toolOptional.isEmpty()) {
                throw new RuntimeException("Tool not found");
            }
            Tool tool = toolOptional.get();
            rentalAgreement.setTool(tool);

            rentalAgreement.setDiscountPercent(Integer.parseInt(discountPercent));
            rentalAgreement.setRentalDayCount(Integer.parseInt(rentalDayCount));
            rentalAgreement.setCheckoutDate(PosUtils.convertToDate(checkoutDate));

            // calculate due date
            rentalAgreement.setDueDate(PosUtils.calculateDueDate(rentalAgreement));

            // calculate charge days
            rentalAgreement.setChargeDays(PosUtils.calculateChargeDays(rentalAgreement));

            // calculate pre-discount charge
            rentalAgreement.setPreDiscountCharge(PosUtils.calculatePreDiscountCharge(rentalAgreement));

            // calculate discount amount
            rentalAgreement.setDiscountAmount(PosUtils.calculateDiscountAmount(rentalAgreement));

            // calculate final charge
            rentalAgreement.setFinalCharge(PosUtils.calculateFinalCharge(rentalAgreement));

            // add to rental agreements table
            rentalAgreementRepository.save(rentalAgreement);
            ;
            returnCode = HttpStatus.OK;
        } catch (Exception e) {
            rentalAgreement.setErrorMsg(e.getMessage());
            returnCode = HttpStatus.NOT_ACCEPTABLE;
        }
        return ResponseEntity.status(returnCode).body(rentalAgreement);

    }

    @GetMapping("/printReceipt/{id}")
    public ResponseEntity<RentalAgreement> printReceipt(@PathVariable Long id) {
        try {
            RentalAgreement rentalAgreement = rentalAgreementRepository.findById(id).orElseThrow(() -> new RuntimeException("Rental Agreement not found"));
            return ResponseEntity.status(HttpStatus.OK).body(rentalAgreement);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/health-check")
    public String getHealthCheck() {
        return "Situation Normal All Fired Up";
    }
}
