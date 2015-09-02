# Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
#
#     http://aws.amazon.com/apache2.0/
#
# or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

#!/usr/bin/python

import sys, argparse, logging
import requests
import boto.ec2


__author__ = 'stormacq'
__version__ = 1.0

'''
Usage : findAMI --region <region_name>
                --version <version>
                --locale <two letters locale code> (default: 'en')

Find an Amazon's Windows Base AMI in the given region, the given version and for the given locale.
This command only search for 64bits EBS based AMI

Example : findAMI --region eu-west-1 --version '2012'
'''

class AMIFinder:

    def __init__(self, logger=None):

        self.logger = logger or logging.getLogger(self.__class__.__name__)


        self.locales = { 'en' : 'English',
                         'fr' : 'French' }

        #shared connection object - but do not cache it as it is region dependant
        self.conn = None

        self.DEFAULT_FILTERS = { 'platform'           : 'windows',
                                 'architecture'       : 'x86_64',
                                 'root-device-type'   : 'ebs'}

        #cached complete AMI list for DEFAULT_FILTER
        self.amiList = None

    def searchDescription(self, image, search, locale):
        '''
            Search for matching string values in the image Description attribute

            This search function searches against the Base AMI only
        '''
        result = None

        if image.description is not None:
            if image.description.find('Base AMI') > -1:
                if image.description.find(locale) > -1:
                    if image.description.find(search) > -1:
                        result = image

        return result


    def findWindowsAMIInRegion(self, region, searchCriteria, locale='en'):
        '''
            Search for a Amazon's Base AMI Windows, 64 bits, EBS based in the specific region,
            the specific searchCriteria and the specific locale

            Typically, searchCriteria is the Windows version number (2012, 2008 SP1 ...)
        '''

        l = self.locales.get(locale);
        if l is None:
            self.logger.error('Unknown locale : %s' % locale)
            return None

        if boto.ec2.get_region(region) is None:
            self.logger.error('Invalid region : %s' % region)
            return None

        self.conn = boto.ec2.connect_to_region(region)

        if self.conn is None:
            self.logger.error('Can not connect to AWS')
            return None

        if (self.amiList is None):
            self.amiList = self.conn.get_all_images(owners='amazon', filters=self.DEFAULT_FILTERS)
            self.logger.debug('Retrieved %d images' % len(self.amiList))

            if len(self.amiList) < 1:
                self.logger.warning('No image retrieved for region "%s" using default filters (Windows, 64 Bits, EBS)' % region)
                return None

        result = None
        for image in self.amiList:
            #print image.description
            if self.searchDescription(image, searchCriteria, l) is not None:
                #print vars(image)
                result = image

        if result is not None:
            self.logger.debug('ImageID: %s ImageDescription: %s', result.id, result.description)
        else:
            self.logger.debug('ImageID: none')

        return result

def main(finder, **kwargs):

    region = kwargs['region']
    if region is None:
        try:
            logger.warning('No region name given, trying to find one from EC2 instance meta data service')
            f = requests.get("http://169.254.169.254/latest/meta-data/placement/availability-zone/", timeout=1)
            region = f.text[:-1]
            logger.info('Using %s as region, provided by instance meta-data' % region)
        except requests.exceptions.Timeout:
            logger.error('Can not find region name (are you running this on EC2 ?). Abording.')
            sys.exit(-1)
        except:
            logger.error('Unknown error while trying to get region name. Abording.')
            sys.exit(-1)

    ami = finder.findWindowsAMIInRegion(region, kwargs['amiversion'], kwargs['locale'])
    if ami is not None:
        print ami.id
        sys.exit(0)
    else:
        sys.exit(-1)


if __name__ == '__main__':

    logging.basicConfig()
    logger = logging.getLogger('findAMI')
    logger.setLevel(logging.INFO)
    finder = AMIFinder(logger)

    if sys.version_info < (2, 7):
        logger.info('Using Python < 2.7')
        parser = argparse.ArgumentParser(description='Find an Amazon Windows Base AMI')
        parser.add_argument('-v', '--version', action='version', version='%(prog)s v' +
        str(__version__))
    else:
        parser = argparse.ArgumentParser(description='Find an Amazon Windows Base AMI', version='%(prog)s v' +
                                                                                            str(__version__))
    parser.add_argument('-r', '--region', type=str, help='Region name (default to local region when run on EC2)')
    parser.add_argument('-a', '--amiversion', type=str, help='String to search in version name (for example "2008" or '
                                                          '"2012 SP1")', required=True)
    parser.add_argument('-l', '--locale', type=str, default='en', help='Two letters locale name')
    args = parser.parse_args()
    main(finder, **vars(args))
