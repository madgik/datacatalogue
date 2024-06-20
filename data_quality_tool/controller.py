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
    print("Home endpoint accessed")
    return "Welcome to the Excel-JSON Converter API!"


@app.route("/excel-to-json", methods=["POST"])
def excel_to_json():
    print("excel_to_json endpoint accessed")
    if "file" not in request.files:
        print("No file part in request")
        return jsonify({"error": "No file part"}), 400
    file = request.files["file"]
    if file.filename == "":
        print("No selected file")
        return jsonify({"error": "No selected file"}), 400
    if file:
        print(f"Processing file: {file.filename}")
        file_stream = BytesIO(file.read())
        df = pd.read_excel(file_stream, engine="openpyxl")
        print("Excel file read into DataFrame")
        excel_validator.validate_excel(df)
        print("Excel file validated")
        json_data = convert_excel_to_json(df)
        print("Excel file converted to JSON")
        pretty_json_data = json.dumps(json_data, indent=4)
        print("JSON data pretty-printed")
        response = app.response_class(
            response=pretty_json_data,
            status=200,
            mimetype='application/json'
        )
        return response

@app.route("/json-to-excel", methods=["POST"])
def json_to_excel():
    print("json_to_excel endpoint accessed")
    if not request.json:
        print("No JSON provided in request")
        return "Please provide the json", 400
    json_data = request.json
    print(f"Processing JSON data: {json_data}")
    json_validator.validate_json(json_data)
    print("JSON data validated")
    df = convert_json_to_excel(json_data)
    print("JSON data converted to Excel")
    output = BytesIO()
    with pd.ExcelWriter(output, engine="xlsxwriter") as writer:
        df.to_excel(writer, index=False)
    output.seek(0)
    print("Excel file created and saved to buffer")
    return send_file(
        output,
        as_attachment=True,
        download_name="output.xlsx",
        mimetype="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    )

@app.route("/validate-json", methods=["POST"])
def validate_json():
    print("validate_json endpoint accessed")
    json_data = request.json
    if not request.json:
        print("No JSON provided in request")
        return "Please provide the json", 400
    try:
        json_validator.validate_json(json_data)
        print("JSON data is valid")
        return jsonify({"message": "Data model is valid."})
    except json_validator.InvalidDataModelError as e:
        print(f"JSON validation error: {str(e)}")
        return jsonify({"error": str(e)}), 400

@app.route("/validate-excel", methods=["POST"])
def validate_excel():
    print("validate_excel endpoint accessed")
    if "file" not in request.files:
        print("No file part in request")
        return jsonify({"error": "No file part"}), 400
    file = request.files["file"]
    if file.filename == "":
        print("No selected file")
        return jsonify({"error": "No selected file"}), 400
    if file:
        print(f"Processing file: {file.filename}")
        file_stream = BytesIO(file.read())
        df = pd.read_excel(file_stream, engine="openpyxl")
        print("Excel file read into DataFrame")
        try:
            excel_validator.validate_excel(df)
            print("Excel file is valid")
            return jsonify({"message": "Data model is valid."})
        except json_validator.InvalidDataModelError as e:
            print(f"Excel validation error: {str(e)}")
            return jsonify({"error": str(e)}), 400

if __name__ == '__main__':
    print("Starting Flask server...")
    app.run(host='0.0.0.0', port=8000)
