module.exports = function buildRenterVerificationTemplate({ payload }) {
  return {
    text: [
      "[INFO] Renter verification result",
      `Renter ID: ${payload.renterId}`,
      `Renter name: ${payload.renterName}`,
      `Status: ${payload.status}`,
      `Reason: ${payload.reason || "N/A"}`
    ].join("\n")
  };
};
