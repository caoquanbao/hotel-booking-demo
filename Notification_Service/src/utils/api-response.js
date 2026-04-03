function success(data, message = "Success") {
  return {
    success: true,
    message,
    data
  };
}

function error(message = "Error", details = null) {
  return {
    success: false,
    message,
    details
  };
}

module.exports = {
  success,
  error
};
