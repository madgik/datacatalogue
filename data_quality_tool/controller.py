import json

from flask import Flask, request, jsonify, send_file
from flask_cors import CORS
import pandas as pd
from io import BytesIO

from converter.excel_to_json import convert_excel_to_json
from converter.json_to_excel import convert_json_to_excel
from validator import json_validator, excel_validator

app = Flask(__name__)
CORS(app)

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
        excel_validator.validate_excel(df)
        # Read the Excel file into a Pandas DataFrame
        json_data = convert_excel_to_json(df)

        # Pretty-print the JSON data
        pretty_json_data = json.dumps(json_data, indent=4)

        response = app.response_class(
            response=pretty_json_data,
            status=200,
            mimetype='application/json'
        )
        return response

@app.route("/json-to-excel", methods=["POST"])
def json_to_excel():
    if not request.json:
        return "Please provide the json", 400
    json_data = request.json
    df = convert_json_to_excel(json_data)
    json_validator.validate_json(json_data)
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

@app.route("/validate-json", methods=["POST"])
def validate_json():
    json_data = request.json
    if not request.json:
        return "Please provide the json", 400
    try:
        json_validator.validate_json(json_data)
        return jsonify({"message": "Data model is valid."})
    except json_validator.InvalidDataModelError as e:
        return jsonify({"error": str(e)}), 400

@app.route("/validate-excel", methods=["POST"])
def validate_excel():
    if "file" not in request.files:
        return jsonify({"error": "No file part"}), 400
    file = request.files["file"]
    if file.filename == "":
        return jsonify({"error": "No selected file"}), 400
    if file:
        file_stream = BytesIO(file.read())
        # Use the file_stream object with pandas
        df = pd.read_excel(file_stream, engine="openpyxl")
        try:
            excel_validator.validate_excel(df)
            return jsonify({"message": "Data model is valid."})
        except json_validator.InvalidDataModelError as e:
            return jsonify({"error": str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8000)
