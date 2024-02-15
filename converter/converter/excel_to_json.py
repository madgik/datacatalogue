import pandas as pd
import json

# Mapping of Excel columns to JSON keys, adjust as necessary,
# Did not contain the values because it is not in a 1 to 1 scenario.
EXCEL_JSON_FIELDS_MAP = {
    "name": "label",
    "code": "code",
    "type": "type",
    "unit": "units",
    "description": "description",
    "canBeNull": "canBeNull",
    "comments": "comments",
    "conceptPath": "conceptPath",
    "methodology": "methodology",
}

TYPE_2_SQL = {
    "nominal": ("text", True),
    "real": ("real", False),
    "integer": ("int", False),
    "text": ("text", False),
}


def process_enumerations(enumerations_str):
    """
    Parses a custom-formatted string into a list of dictionaries with 'code' and 'label'.
    Expected format: '{"code1", "label1"}, {"code2", "label2"}'.
    """
    try:
        enumerations_str = (
            "[" + enumerations_str.replace('","', '":"').replace('", "', '": "') + "]"
        )
        parsed = json.loads(enumerations_str)
    except json.decoder.JSONDecodeError:
        raise ValueError(
            "Could not parse enumerations: "
            + enumerations_str
            + '"proper format is {"code1", "label1"}, {"code2", "label2"}"'
        )
    return [
        {"code": list(item.keys())[0], "label": list(item.values())[0]}
        for item in parsed
    ]


def insert_variable_into_structure(root, variable, path):
    """
    Insert a variable into the hierarchical structure based on the provided path.
    """
    # The last path element of the list is always the variable.
    for part in path[:-1]:
        # Find or create the group at the current level
        found = False
        for group in root["groups"]:
            if group["code"] == part:
                root = group
                found = True
                break

        if not found:
            new_group = {"code": part, "label": part, "groups": [], "variables": []}
            root["groups"].append(new_group)
            root = new_group

    root["variables"].append(variable)


def process_values_based_on_type(row, variable):
    """
    Processes the 'values' field based on the variable's 'type':
    - For 'integer' or 'real', extracts 'minValue' and 'maxValue' from a range specified in 'values'
      and ensures these values are of the appropriate type.
    - For 'nominal', retrieves a list of 'enumerations' from 'values'.
    """
    code = row.get("code")
    values = row.get("values")
    variable_type = row.get("type")

    if variable_type in ["real", "integer"]:
        if not values:
            return

        # Split the range and strip whitespace
        try:
            print(values)
            min_value, max_value = map(str.strip, values.split("-"))
        except ValueError:
            raise ValueError(f"Invalid range format for variable {code}: {values}")

        # Convert min and max values to the appropriate type
        try:
            if variable_type == "integer":
                variable["minValue"], variable["maxValue"] = int(min_value), int(
                    max_value
                )
            elif variable_type == "real":
                variable["minValue"], variable["maxValue"] = float(min_value), float(
                    max_value
                )
        except ValueError:
            raise ValueError(
                f"Range values for variable {code} must be valid {variable_type} numbers"
            )

    elif variable_type == "nominal":
        if not values:
            raise ValueError(
                f"The 'values' should not be empty for variable {code} when type is 'nominal'"
            )
        variable["enumerations"] = process_enumerations(values)


def validate_variable_type(row):
    valid_types = set(TYPE_2_SQL.keys())
    if "type" not in row or row["type"] not in valid_types:
        valid_types_str = ", ".join(valid_types)
        raise ValueError(
            f"The row must have a 'type' field with a valid value."
            f" Valid values are: {valid_types_str}, got '{row.get('type')}' instead."
        )


def process_variable(row):
    """
    Processes a single row into a variable dictionary, applying validations
    and transformations based on the row's data.
    """
    # Validate variable type first
    validate_variable_type(row)

    # Initialize the variable dictionary with mappings from EXCEL_JSON_FIELDS_MAP
    variable = {
        json_key: row[excel_col]
        for excel_col, json_key in EXCEL_JSON_FIELDS_MAP.items()
        if excel_col in row and pd.notnull(row[excel_col])
    }

    # Process 'values' based on variable type, which might modify 'variable' in-place
    process_values_based_on_type(row, variable)

    variable["sql_type"], variable["isCategorical"] = TYPE_2_SQL[variable["type"]]

    return variable


def convert_excel_to_json(df):
    """
    Converts a DataFrame from Excel into a JSON structure, handling enumerations specifically,
    and adds 'isCategorical' and 'sql_type' based on the 'type'.
    """
    root = {"variables": [], "groups": [], "code": "root"}

    for _, row in df.iterrows():
        try:

            variable = process_variable(row.to_dict())
            if "conceptPath" in variable and variable["conceptPath"]:
                path = variable["conceptPath"].split("/")
                del variable["conceptPath"]
                insert_variable_into_structure(root, variable, path)
            else:
                raise ValueError(
                    f"The variable {variable['code']} is missing the conceptPath"
                )
        except ValueError as e:
            raise ValueError(f"Error processing variable: {e}")

    if root["groups"]:
        data_model = root["groups"][0]
        data_model["version"] = "to be defined"
        return data_model
    else:
        return {"code": "No groups found", "groups": [], "variables": root["variables"]}
