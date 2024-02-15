import json
import unittest
from io import BytesIO

import pandas as pd

from controller import app


class TestExcelJsonConverterAPI(unittest.TestCase):
    def setUp(self):
        self.app = app
        self.app.testing = True
        self.client = self.app.test_client()

    def test_home_route(self):
        response = self.client.get("/")
        self.assertEqual(response.status_code, 200)
        self.assertIn(
            "Welcome to the Excel-JSON Converter API!", response.data.decode()
        )

    def test_excel_to_json_conversion(self):
        with open("MinimalDataModelExample.xlsx", "rb") as file:
            data = {"file": (file, "MinimalDataModelExample.xlsx")}
            response = self.client.post(
                "/excel-to-json", content_type="multipart/form-data", data=data
            )
            self.assertEqual(response.status_code, 200)
            minimal_data_model = json.loads(response.data.decode())
            # Validate Data Model
            expected_data_model_fields = [
                "code",
                "groups",
                "label",
                "variables",
                "version",
            ]
            self.assertListEqual(
                list(minimal_data_model.keys()), expected_data_model_fields
            )
            self.assertEqual(minimal_data_model["code"], "Minimal Example")

            # Validate dataset in the variables
            self.assertEqual(len(minimal_data_model["variables"]), 1)
            expected_columns = [
                "code",
                "description",
                "enumerations",
                "isCategorical",
                "label",
                "methodology",
                "sql_type",
                "type",
            ]
            self.assertListEqual(
                list(minimal_data_model["variables"][0].keys()), expected_columns
            )
            self.assertEqual(minimal_data_model["variables"][0]["code"], "dataset")

            # Validate Group
            self.assertEqual(len(minimal_data_model["groups"]), 1)
            expected_group_fields = ["code", "groups", "label", "variables"]
            self.assertListEqual(
                list(minimal_data_model["groups"][0].keys()), expected_group_fields
            )
            self.assertEqual(minimal_data_model["groups"][0]["code"], "Example Group")

            # Validate Variable of the group
            self.assertEqual(len(minimal_data_model["groups"]), 1)
            expected_columns = [
                "code",
                "description",
                "isCategorical",
                "label",
                "maxValue",
                "methodology",
                "minValue",
                "sql_type",
                "type",
            ]
            self.assertListEqual(
                list(minimal_data_model["groups"][0]["variables"][0].keys()),
                expected_columns,
            )
            self.assertEqual(
                minimal_data_model["groups"][0]["variables"][0]["code"],
                "group_variable",
            )

            # Validate Sub-Group
            self.assertEqual(len(minimal_data_model["groups"][0]["groups"]), 1)
            self.assertListEqual(
                list(minimal_data_model["groups"][0]["groups"][0].keys()),
                expected_group_fields,
            )
            self.assertEqual(
                minimal_data_model["groups"][0]["groups"][0]["code"], "Nested Group"
            )

            # Validate Variable of the group
            self.assertEqual(len(minimal_data_model["groups"][0]["groups"]), 1)
            expected_sub_group_variable_columns = [
                "code",
                "description",
                "enumerations",
                "isCategorical",
                "label",
                "methodology",
                "sql_type",
                "type",
            ]
            self.assertListEqual(
                list(
                    minimal_data_model["groups"][0]["groups"][0]["variables"][0].keys()
                ),
                expected_sub_group_variable_columns,
            )
            self.assertEqual(
                minimal_data_model["groups"][0]["groups"][0]["variables"][0]["code"],
                "nested_group_variable",
            )

    def test_json_to_excel_conversion(self):
        with open("MinimalDataModelExample.json", "r") as file:
            json_data = json.load(file)
        response = self.client.post(
            "/json-to-excel", json=json_data, content_type="application/json"
        )
        self.assertEqual(response.status_code, 200)
        self.assertEqual(
            response.content_type,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        )

        # Read the Excel file from the response
        excel_file = BytesIO(response.data)
        df = pd.read_excel(excel_file)
        excel_columns = [
            "csvFile",
            "name",
            "code",
            "type",
            "values",
            "units",
            "description",
            "canBeNull",
            "comments",
            "conceptPath",
            "methodology",
        ]
        self.assertListEqual(list(df.columns), excel_columns)
        self.assertEqual(
            df["name"].tolist(),
            ["Dataset Variable", "Group Variable", "Nested Group Variable"],
        )
        self.assertEqual(
            df["code"].tolist(), ["dataset", "group_variable", "nested_group_variable"]
        )
        self.assertEqual(df["type"].tolist(), ["nominal", "integer", "nominal"])
        self.assertEqual(
            df["values"].tolist(),
            [
                '{"enum1","Enumeration 1"}',
                "0-100",
                '{"nested_enum1","Nested Enumeration 1"}',
            ],
        )
        self.assertEqual(
            df["conceptPath"].tolist(),
            [
                "Minimal Example/dataset",
                "Minimal Example/Example Group/group_variable",
                "Minimal Example/Example Group/Nested Group/nested_group_variable",
            ],
        )
        self.assertEqual(
            df["methodology"].tolist(),
            ["example methodology", "group methodology", "nested methodology"],
        )
