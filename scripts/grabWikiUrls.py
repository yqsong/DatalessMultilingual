import os
import sys

import argparse
import urllib
from BeautifulSoup import BeautifulSoup, SoupStrainer
import httplib
import httplib
from urlparse import urlparse
# AUTHOR: Shyam shyamupa@gmail.com
# creates a file with urls, wikiurls.txt, which contains the download urls for all wikipedias (all languages)
# once this script generates wikiurls.txt, you can
# wget --no-clobber -i wikiurls.txt
# wiki.html is the html page "https://dumps.wikimedia.org/backup-index.html"

def checkUrl(url):
            "checks if the url is dead or not"
            p = urlparse(url)
            conn = httplib.HTTPConnection(p.netloc)
            conn.request('HEAD', p.path)
            resp = conn.getresponse()
            return resp.status < 400

f=open('wiki.html').read()
out=open('wikiurls.txt','w')
year="20150826"

count=0
for link in BeautifulSoup(f, parseOnlyThese=SoupStrainer('a')):
            if link.text.endswith('wiki') and len(link.text)==6:
                        dwnload_link = "https://dumps.wikimedia.org/%s/%s/%s-%s-pages-articles.xml.bz2" % (link.text,year,link.text,year)
                        if checkUrl(dwnload_link):
                                    print >> out, dwnload_link
                                    count+=1
                                    # sys.exit(-1)
print count
out.close()
