# coding: utf-8
import goslate
import sys
import time

myargs = sys.argv
myfile = myargs[1]
lang = myargs[2]

path = "/shared/bronte/hpeng7/multilingual-embedding/selected_original/";
text = open(path + myfile, 'r').read()

gs = goslate.Goslate()
print gs.translate(text, lang).encode('utf-8')

#files = [path + "selected_original/" + line.rstrip('\n') for line in open(path + "filelist.txt", "r")]

#langs=["de","fr","ar","ru","es"]
#lang = "de"

#for myfile in files:
#	text = open(myfile, 'r').read()
#	translation = get_word(text, lang)
#	print translation
#	time.sleep(15)
