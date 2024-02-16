from flask import Flask, request, jsonify, send_file
import pandas as pd
from io import BytesIO

from converter.excel_to_json import convert_excel_to_json
from converter.json_to_excel import convert_json_to_excel

app = Flask(__name__)


@app.route("/")
def home():
    return "Welcome to the Excel-JSON Converter API!"


@app.route("/excel-to-json", methods=["POST"])
def excel_to_json():
    if "file" not in request.files:
        return jsonify({"error": "No file part"}), 400
    file = request.files["file"]
    if file.filename == "":
        return jsonify({"error": "No selected file"}), 400
    if file:
        file_stream = BytesIO(file.read())
        # Use the file_stream object with pandas
        df = pd.read_excel(file_stream, engine="openpyxl")
        # Read the Excel file into a Pandas DataFrame
        df = df.astype(str).replace('nan', None)
        json_data = convert_excel_to_json(df)
        return jsonify(json_data)


@app.route("/json-to-excel", methods=["POST"])
def json_to_excel():
    if not request.json:
        return "", 400
    json_data = request.json
    df = convert_json_to_excel(json_data)
    # Create a BytesIO buffer to save the Excel file
    output = BytesIO()
    with pd.ExcelWriter(output, engine="xlsxwriter") as writer:
        df.to_excel(writer, index=False)
    output.seek(0)
    # Send the generated Excel file
    return send_file(
        output,
        as_attachment=True,
        download_name="output.xlsx",  # Use download_name for newer Flask versions if attachment_filename causes issues
        mimetype="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    )
