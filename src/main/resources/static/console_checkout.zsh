#!/bin/zsh
curl --location 'http://localhost:8080/api/checkout' \
--header 'Content-Type: application/json' \
--data '{
    "toolCode":"JAKR",
    "rentalDayCount":"15",
    "discountPercent":"10",
    "checkoutDate":"09/03/2015"
}' | jq -r '
  if .errorMsg then
    "\(.errorMsg)"
  else
    "Tool code: \(.tool.toolCode)",
    "Tool type: \(.tool.toolType)",
    "Brand: \(.tool.brand)",
    "id: \(.id)",
    "Rental days: \(.rentalDayCount)",
    "Check out date: \(.checkoutDate | strptime("%Y-%m-%d") | strftime("%m/%d/%y"))",
    "Due date: \(.dueDate | strptime("%Y-%m-%d") | strftime("%m/%d/%y"))",
    "Daily rental charge: $\(.tool.brandCharge.dailyRentalCharge)",
    "Charge days: \(.chargeDays)",
    "Pre-discount charge: $\(.preDiscountCharge)",
    "Discount percent: \(.discountPercent)%",
    "Discount amount: $\(.discountAmount)",
    "Final charge: $\(.finalCharge)"
  end
'