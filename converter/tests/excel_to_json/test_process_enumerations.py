import unittest

from converter.excel_to_json import process_enumerations, process_values_based_on_type


class TestProcessEnumerations(unittest.TestCase):
    def test_valid_input(self):
        input_str = '{"code1", "label1"}, {"code2", "label2"}'
        expected_output = [
            {"code": "code1", "label": "label1"},
            {"code": "code2", "label": "label2"},
        ]
        result = process_enumerations(input_str)
        self.assertEqual(result, expected_output)

    def test_empty_input(self):
        input_str = ""
        expected_output = []
        result = process_enumerations(input_str)
        self.assertEqual(result, expected_output)

    def test_malformed_input_raises_error(self):
        input_str = '{"code1" "label1"}, {"code2": "label2"}'
        with self.assertRaises(ValueError):
            process_enumerations(input_str)

    def test_with_escaped_characters(self):
        input_str = '{"code1", "label with \\"escaped quotes\\""}, {"code2", "label2"}'
        expected_output = [
            {"code": "code1", "label": 'label with "escaped quotes"'},
            {"code": "code2", "label": "label2"},
        ]
        result = process_enumerations(input_str)
        self.assertEqual(result, expected_output)

    def test_with_special_characters(self):
        input_str = (
            '{"code1", "label1 with special &*() characters"}, {"code2", "label2"}'
        )
        expected_output = [
            {"code": "code1", "label": "label1 with special &*() characters"},
            {"code": "code2", "label": "label2"},
        ]
        result = process_enumerations(input_str)
        self.assertEqual(result, expected_output)

    def test_single_enumeration(self):
        input_str = '{"code1", "label1"}'
        expected_output = [{"code": "code1", "label": "label1"}]
        result = process_enumerations(input_str)
        self.assertEqual(result, expected_output)

    def test_incorrect_format_raises_error(self):
        input_str = "Not a valid format at all"
        with self.assertRaises(ValueError):
            process_enumerations(input_str)

    def test_non_standard_input_variation(self):
        input_str = '{"code1", "label1"}, {"code2", "label2 with, comma"}'
        expected_output = [
            {"code": "code1", "label": "label1"},
            {"code": "code2", "label": "label2 with, comma"},
        ]
        result = process_enumerations(input_str)
        self.assertEqual(result, expected_output)

    def test_nominal_with_incorrect_range_values(self):
        row = {"type": "nominal", "values": "1-100", "code": "NominalWithRange"}
        variable = {}
        with self.assertRaises(ValueError) as context:
            process_values_based_on_type(row, variable)
        self.assertIn("Could not parse enumerations:", str(context.exception))
