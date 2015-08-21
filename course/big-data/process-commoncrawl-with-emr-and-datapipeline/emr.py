# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


#!/usr/bin/python

import boto.dynamodb
import subprocess

conn = boto.dynamodb.connect_to_region('<your-region>')
table = conn.get_table('<your-dynamo-table>')

cat = subprocess.Popen(["hadoop", "fs", "-cat", "/grep-output/part-r-00000"], stdout=subprocess.PIPE)

item_phrase = ""
item_count = ""

tup_bag = []
for line in cat.stdout:
    split_line = line.strip('\n').split('\t')
    #In case of empty lines
    if split_line[0] == "":
        break
    item_count = split_line[0]
    item_phrase = split_line[1]
    item_data = { 'count' : int(item_count)}
    item = table.new_item(hash_key=item_phrase,attrs=item_data)
    item.save()
