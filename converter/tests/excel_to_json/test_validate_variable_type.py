import unittest

from converter.excel_to_json import TYPE_2_SQL, validate_variable_type


class TestValidateVariableType(unittest.TestCase):

    def test_with_valid_type(self):
        for valid_type in TYPE_2_SQL.keys():
            row = {"type": valid_type}
            validate_variable_type(row)  # Should not raise an error

    def test_with_invalid_type(self):
        row = {"type": "unsupported_type"}
        with self.assertRaises(ValueError) as context:
            validate_variable_type(row)
        self.assertTrue(
            "The row must have a 'type' field with a valid value."
            in str(context.exception)
        )

    def test_missing_type_field(self):
        row = {"name": "Variable without type"}
        with self.assertRaises(ValueError) as context:
            validate_variable_type(row)
        self.assertTrue(
            "The row must have a 'type' field with a valid value."
            in str(context.exception)
        )
