import cv2
import numpy as np
import math
import os

import win32com.client
import pyttsx

class OpenCVHandler(object):

	def get_text_translation_from_image(self, image_name, image_id):
		# read captured image
		cropped_image = self.read_captured_image_and_crop(image_name)

		cv2.imshow('im', cropped_image)

		# Write cropped image to server for debugging purposes.
		cropped_image_path = 'cropped_image_%s.jpg' % (image_id)
		cv2.imwrite(cropped_image_path, cropped_image)

		# Get grey scale image
		grey_image = cv2.cvtColor(cropped_image, cv2.COLOR_BGR2GRAY)

		contours = self.get_contours_of_image(grey_image)
		image_largest_contour = self.get_largest_contour(contours)
		stock_image_paths = self.get_paths_to_stored_images()
		curr_dict = self.get_correlations_with_stock_images(stock_image_paths)

		x, y, w, h = cv2.boundingRect(image_largest_contour)
		sketch = np.zeros(cropped_image.shape, np.uint8)
		cv2.drawContours(sketch,[image_largest_contour], 0, (0, 255, 0), 0)
		cv2.imshow("Contour", sketch)

		os.remove(cropped_image_path)
		return get_letter_with_lowest_correlation(corr_dict)

	def get_correlations_with_stock_images(stock_image_paths):
		# Iterate through each image path
		# Get each image contour
		# Compare to the received image's contour
		# Put that correlation into dictionary
		corr_dict = {}
		for stock_image_path in stock_image_paths:
			# Get letter
			pieces = stock_image_path.split('/')
			letter = pieces[0].split('_')[1]
			stock_image = cv2.imread(stock_image_path)

			stock_grey_image = cv2.cvtColor(stock_image, cv2.COLOR_BGR2GRAY)
			contours = self.get_contours_of_image(stock_grey_image)
			stock_largest_contour = self.get_largest_contour(contours)

			corr = self.compare_shapes(stock_largest_contour, image_largest_contour)
			corr_dict[corr] = letter

		return corr_dict

	def get_letter_with_lowest_correlation(corr_dict):
		for key in corr_dict:
			print '%s : %s' % (corr_dict[key], key)

		corr_list = corr_dict.keys()
		corr_list.sort()

		return corr_dict[curr_list[0]]

	def compare_shapes(self, shape1, shape2):
		return cv2.matchShapes(shape1, shape2, 1, 0.0)

	def get_largest_contour(self, contours):
		largest_contour = max(contours, key = lambda x: cv2.contourArea(x))

		return largest_contour

	def get_contours_of_image(self, image):
		(version, _, _) = cv2.__version__.split('.')
		value = (35, 35)

		blurred = cv2.GaussianBlur(image, value, 0)
		_, letter_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

		if version is '3':
			image, contours, hierarchy = cv2.findContours(letter_thresh.copy(), \
			   cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

		elif version is '2':
			contours, hierarchy = cv2.findContours(letter_thresh.copy(),cv2.RETR_TREE, \
			   cv2.CHAIN_APPROX_NONE)

		return contours


	def get_paths_to_stored_images(self):
		paths = []
		for letter_value in range(ord('A'), ord('G')):
			folder_path = 'sign_%s' % (chr(letter_value))
			directory = os.listdir(folder_path)
			for image_name in directory:
				file_path = '%s/%s' % (folder_path, image_name)
				paths.append(file_path)

		return paths

	def read_captured_image_and_crop(self, image_name):
		img = cv2.imread(image_name)
		height, width, channels = img.shape
		cv2.rectangle(img,(width / 2, height / 3),(width / 5,0),(0,255,0),0)
		cropped_image = img[0:(height / 3), (width / 5):(width / 2)]

		return cropped_image

	def play_audio_translation_from_text(self, text):
		eng = pyttsx.init()
		eng.say(text)
		eng.runAndWait()
