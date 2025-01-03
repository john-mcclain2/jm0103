#!/bin/zsh

# Extract the uniqueIdentifier from the URL and store it in a variable
id="1"

curl --location "http://localhost:8080/api/printReceipt/$id" \
 | jq -r '
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
