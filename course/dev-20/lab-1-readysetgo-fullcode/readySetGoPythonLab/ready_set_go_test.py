#Copyright 2015 Amazon Web Services, Inc. or its affiliates. All rights reserved.

from unittest import TestCase
import unittest

from ready_set_go import getAllBuckets


class TestReadySetGo(TestCase):

    # Test to see if a list of buckets are returned
    def test_get_all_buckets(self):
        no_of_buckets = getAllBuckets()
        self.assertTrue(self, no_of_buckets >= 0)


if __name__ == '__main__':
    unittest.main()
