# OpenCV imports go here
import cv2
import numpy as np
import math

import win32com.client
import pyttsx

class OpenCVHandler(object):

	# target rect coordinates (250,275),(50,0)
	@staticmethod
	def get_text_translation_from_image(image_name):

		(version, _, _) = cv2.__version__.split('.')
		value = (35, 35)
		corrDict = {}

		# read captured image		
		img = cv2.imread(image_name)
		height, width, channels = img.shape
		print "%s %s" % (width, height)
		cv2.rectangle(img,(width / 2, height / 3),(width / 5,0),(0,255,0),0)

		crop_img = img[0:(height / 3), (width / 5):(width / 2)]
		cv2.imshow('im', crop_img)
		cv2.imwrite('cropped_image.jpg',crop_img)

		grey = cv2.cvtColor(crop_img, cv2.COLOR_BGR2GRAY)
		blurred = cv2.GaussianBlur(grey, value, 0)
		_, thresh1 = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

		# find countours in caputred image
		if version is '3':
		    image, contours, hierarchy = cv2.findContours(thresh1.copy(), \
		           cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

		elif version is '2':
		    contours, hierarchy = cv2.findContours(thresh1.copy(),cv2.RETR_TREE, \
		           cv2.CHAIN_APPROX_NONE)

		# compare captured image with hand 
		for x in range(ord('A'), ord('F')+1):
   			imLetter = cv2.imread('sign_' + chr(x) + '.jpg')
	        grey = cv2.cvtColor(imLetter, cv2.COLOR_BGR2GRAY)
	        blurred = cv2.GaussianBlur(grey, value, 0)
	        _, letter_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

	        if version is '3':
	            image, letter_contours, hierarchy = cv2.findContours(letter_thresh.copy(), \
	               cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

	        elif version is '2':
	            letter_countours, hierarchy = cv2.findContours(letter_thresh.copy(),cv2.RETR_TREE, \
	               cv2.CHAIN_APPROX_NONE)

	        cnt = max(contours, key = lambda x: cv2.contourArea(x))
	        letter_cnt = max(letter_contours, key = lambda x: cv2.contourArea(x))
	    
	        corrDict[cv2.matchShapes(cnt,letter_cnt,1,0.0)] = chr(x) 

		
		x,y,w,h = cv2.boundingRect(cnt)

		sketch = np.zeros(crop_img.shape, np.uint8)
		cv2.drawContours(sketch,[cnt], 0, (0,255,0),0)
		cv2.imshow("contour",sketch)

		
		for key in corrDict:
			print '%s : %s' % (corrDict[key], key)

		corr_list = corrDict.keys()
		corr_list.sort()
		return corrDict[corr_list[0 ]]


	@staticmethod
	def play_audio_translation_from_text(text):
		eng = pyttsx.init()
		eng.say(text)
		eng.runAndWait()
