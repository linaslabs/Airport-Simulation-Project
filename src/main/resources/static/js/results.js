// Sprint 1 mock values
const mock = {
    throughput: 17,
    depAvgWait: 7,
    depMaxQueue: 8,
    depMaxDelay: 15,
    depAvgDelay: 8,
    depCancelled: 2,
    arrAvgHold: 11,
    arrMaxHolding: 6,
    arrMaxDelay: 17,
    arrAvgDelay: 10,
    arrDiverted: 4
};

function setText(id, value){
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function loadMock(){
    setText("throughput", mock.throughput);

    setText("depAvgWait", mock.depAvgWait);
    setText("depMaxQueue", mock.depMaxQueue);
    setText("depMaxDelay", mock.depMaxDelay);
    setText("depAvgDelay", mock.depAvgDelay);
    setText("depCancelled", mock.depCancelled);

    setText("arrAvgHold", mock.arrAvgHold);
    setText("arrMaxHolding", mock.arrMaxHolding);
    setText("arrMaxDelay", mock.arrMaxDelay);
    setText("arrAvgDelay", mock.arrAvgDelay);
    setText("arrDiverted", mock.arrDiverted);
}

function saveResults(){
    alert("Sprint 1: Save Configuration & Data (stub).");
}

function goCompare(){
    alert("Sprint 1: Comparison Menu (stub).");
}

// Wire buttons + init
document.addEventListener("DOMContentLoaded", () => {
    loadMock();

    const saveBtn = document.getElementById("saveBtn");
    const compareBtn = document.getElementById("compareBtn");

    if (saveBtn) saveBtn.addEventListener("click", saveResults);
    if (compareBtn) compareBtn.addEventListener("click", goCompare);
});
