CREATE TABLE Tool (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      toolCode VARCHAR(10),
                      toolType VARCHAR(50) NOT NULL,
                      brand VARCHAR(50) NOT NULL,
                      dailyRentalCharge DECIMAL(5, 2) NOT NULL,
                      weekdayCharge BOOLEAN NOT NULL,
                      weekendCharge BOOLEAN NOT NULL,
                      holidayCharge BOOLEAN NOT NULL
);
CREATE TABLE RentalAgreement (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,

                                 tool_id BIGINT NOT NULL,

                                 rentalDayCount INT,
                                 discountPercent INT,
                                 checkoutDate DATE,
                                 dueDate DATE,
                                 dailyRentalCharge DECIMAL(10, 2),
                                 chargeDays INT,
                                 preDiscountCharge DECIMAL(10, 2),
                                 discountAmount DECIMAL(10, 2),
                                 finalCharge DECIMAL(10, 2),
                                 errorMsg VARCHAR(500),

                                 CONSTRAINT FK_Tool FOREIGN KEY (tool_id) REFERENCES Tool (id) ON DELETE CASCADE
);