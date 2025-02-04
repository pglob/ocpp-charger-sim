let responseList = [];

module.exports = {
  addResponseData(response) {
    responseList.push(response);
  },
  setResponseData(newResponseList) {
    responseList = [];
    newResponseList.forEach(response => {
      responseList.push(response);
    });
  },
  shiftResponseData() {
    return responseList.shift();
  },
  getResponseData() {
    return responseList;
  },
};