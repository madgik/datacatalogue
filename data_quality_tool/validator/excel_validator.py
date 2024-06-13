import json
import re
import pandas as pd

from data_quality_tool.common_entities import InvalidDataModelError, REQUIRED_COLUMNS, EXCEL_COLUMNS, \
    EXCEL_TYPE_2_SQL_TYPE_ISCATEGORICAL_MAP

# Regex for validation
CONCEPT_PATH_PATTERN = r"^[^/]+(/[^/]+)*$"


def validate_enumerations(values):
    # Transform the string to a JSON-compatible format
    try:
        # Transforming {"key","value"} into [{"key": "value"}]
        transformed_values = "[" + values.replace('","', '":"').replace('", "', '": "') + "]"
        enumerations = json.loads(transformed_values)
    except json.JSONDecodeError:
        raise InvalidDataModelError(
            'Nominal values format error: \'{"code", "label"}, {"code", "label"}\' expected but got ' + values + "."
        )
    codes = [code for _enum in enumerations for code, label in _enum.items()]
    if len(codes) != len(set(codes)):
        raise InvalidDataModelError("Duplicate codes found in enumeration values.")


def validate_min_max(values):
    """Validate the format and logic of min-max values."""
    try:
        # Attempt to split the string into exactly two parts and unpack
        min_value, max_value = values.split('-')

        # Try to convert both parts into floats
        float(min_value)
        float(max_value)
    except ValueError:
        raise InvalidDataModelError(
            f"Values must match format '<float or integer>-<float or integer>' but got '{values}'."
        )
    if min_value >= max_value:
        raise InvalidDataModelError("Min value must be smaller than max value.")


def validate_concept_path(concept_path):
    """Validate the format of conceptPath values."""
    if not re.match(CONCEPT_PATH_PATTERN, concept_path):
        raise InvalidDataModelError(
            "ConceptPath format error: 'characters/characters/...' expected."
        )


def validate_variable_type(row):
    """Validate the type and values of a variable based on its type."""
    type_val = row.get("type")
    valid_excel_types = EXCEL_TYPE_2_SQL_TYPE_ISCATEGORICAL_MAP.keys()
    if type_val not in valid_excel_types:
        valid_types_str = ", ".join(valid_excel_types)
        raise InvalidDataModelError(
            f"Invalid 'type': {type_val}. Valid types: {valid_types_str}."
        )
    if type_val == "nominal":
        validate_enumerations(row.get("values", ""))
    elif type_val in ["real", "integer"] and row.get("values"):
        validate_min_max(row["values"])


def validate_variable(row):
    """Validate required columns, variable type, and conceptPath for a single row."""
    for required_col in REQUIRED_COLUMNS:
        if pd.isnull(row[required_col]) or row[required_col] is None:
            raise InvalidDataModelError(
                f"Missing value for required column '{required_col}'."
            )
    validate_variable_type(row)
    validate_concept_path(row["conceptPath"])


def validate_excel(df):
    """Validate the structure and data of an Excel file represented as a DataFrame."""
    df = df.astype(str).replace("nan", None)
    if set(df.columns) != set(EXCEL_COLUMNS):
        missing_excel_columns = set(EXCEL_COLUMNS) - set(df.columns)
        raise InvalidDataModelError(
            "Mismatch in Excel columns. Missing columns: " + ", ".join(missing_excel_columns)
        )
    df.apply(validate_variable, axis=1)
