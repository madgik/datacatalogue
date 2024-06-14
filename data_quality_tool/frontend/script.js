async function convertExcelToJson() {
    const excelFile = document.getElementById('excelFile').files[0];
    if (!excelFile) {
        alert("Please select an Excel file first.");
        return;
    }

    const formData = new FormData();
    formData.append('file', excelFile);

    const response = await fetch('http://127.0.0.1:8000/excel-to-json', {
        method: 'POST',
        body: formData
    });

    if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const downloadLink = document.getElementById('downloadJson');
        downloadLink.href = url;
        downloadLink.download = 'data.json';
        downloadLink.style.display = 'block';
        downloadLink.textContent = 'Download JSON';
    } else {
        alert("Failed to convert Excel to JSON.");
    }
}

async function convertJsonToExcel() {
    const jsonFile = document.getElementById('jsonFile').files[0];
    if (!jsonFile) {
        alert("Please select a JSON file first.");
        return;
    }

    const fileContent = await jsonFile.text();

    const response = await fetch('http://127.0.0.1:8000/json-to-excel', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: fileContent
    });

    if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const downloadLink = document.getElementById('downloadExcel');
        downloadLink.href = url;
        downloadLink.download = 'data.xlsx';
        downloadLink.style.display = 'block';
        downloadLink.textContent = 'Download Excel';
    } else {
        alert("Failed to convert JSON to Excel.");
    }
}
