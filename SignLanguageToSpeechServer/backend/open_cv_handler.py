import cv2
import numpy as np
import math
import os

import win32com.client
import pyttsx

ALLOW_DEBUGING = True

class OpenCVHandler(object):

	def get_text_translation_from_image(self, image_name, image_id):
		# read captured image
		cropped_image = self.read_captured_image_and_crop(image_name)

		# Write cropped image to server for debugging purposes.
		cropped_image_path = 'temp/cropped_image_%s.jpg' % (image_id)
		cv2.imwrite(cropped_image_path, cropped_image)

		# Get grey scale image
		grey_image = cv2.cvtColor(cropped_image, cv2.COLOR_BGR2GRAY)

		contours = self.get_contours_of_image(grey_image)
		image_largest_contour = self.get_largest_contour(contours)
		stock_image_paths = self.get_paths_to_stored_images()
		corr_dict = self.get_correlations_with_stock_images(stock_image_paths,
										image_largest_contour, cropped_image)

		x, y, w, h = cv2.boundingRect(image_largest_contour)
		sketch = np.zeros(cropped_image.shape, np.uint8)
		cv2.drawContours(sketch,[image_largest_contour], 0, (0, 255, 0), 0)
		cv2.imshow("Contour", sketch)

		cv2.imshow('im', cropped_image)

		translation = self.get_letter_with_lowest_correlation(corr_dict)

		print 'Closest Translation: %s' %(translation)
		return translation

	def get_correlations_with_stock_images(self, stock_image_paths,
										image_largest_contour, cropped_image):
		# Iterate through each image path
		# Get each image contour
		# Compare to the received image's contour
		# Put that correlation into dictionary

		num_defects = self.find_defects(image_largest_contour, cropped_image)
		corr_dict = {}
		# The list is for debugging purposes.
		corr_list = []

		defect_map = self.get_defect_map()
		for stock_image_path in stock_image_paths:
			# Get letter
			pieces = stock_image_path.split('/')
			letter = pieces[1].split('_')[1]
			stock_image = cv2.imread(stock_image_path)

			#if self.within_two(num_defects, defect_map[letter]):
			stock_grey_image = cv2.cvtColor(stock_image, cv2.COLOR_BGR2GRAY)
			contours = self.get_contours_of_image(stock_grey_image)
			stock_largest_contour = self.get_largest_contour(contours)

			corr = self.compare_shapes(stock_largest_contour,
														image_largest_contour)
			corr_dict[corr] = letter
			corr_list.append('%s\t-- %s' % (corr, letter))

		if ALLOW_DEBUGING:
			self.pretty_print_corr_list(corr_list)

		return corr_dict

	def pretty_print_corr_list(self, corr_list):
		corr_list.sort()
		for item in corr_list:
			print '%s' % (item)

	def get_letter_with_lowest_correlation(self, corr_dict):
		corr_list = corr_dict.keys()
		corr_list.sort()

		return corr_dict[corr_list[0]]

	def compare_shapes(self, shape1, shape2):
		return cv2.matchShapes(shape1, shape2, 2, 0.0)

	def get_largest_contour(self, contours):
		largest_contour = max(contours, key = lambda x: cv2.contourArea(x))
		return largest_contour

	def get_defect_map(self):
		return {'A': 0, 'B': 0, 'C': 2, 'D': 0,'E': 0,'F': 1,'G': 1}

	def get_contours_of_image(self, image):
		(version, _, _) = cv2.__version__.split('.')
		value = (35, 35)

		blurred = cv2.GaussianBlur(image, value, 0)
		_, letter_thresh = cv2.threshold(blurred, 127, 255,
										cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

		if version is '3':
			image, contours, hierarchy = cv2.findContours(letter_thresh.copy(),
										cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

		elif version is '2':
			contours, hierarchy = cv2.findContours(letter_thresh.copy(),
										cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

		return contours


	def get_paths_to_stored_images(self):
		paths = []
		for letter_value in range(ord('A'), ord('F') + 1):
			folder_path = 'letters/sign_%s' % (chr(letter_value))
			directory = os.listdir(folder_path)
			for image_name in directory:
				if not image_name.endswith('.jpg'):
					continue
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

	def find_defects(self, cnt, crop_img):
		hull = cv2.convexHull(cnt,returnPoints = False)
		defects = cv2.convexityDefects(cnt,hull)
		count_defects = 0
		for i in range(defects.shape[0]):
			s,e,f,d = defects[i,0]
			start = tuple(cnt[s][0])
			end = tuple(cnt[e][0])
			far = tuple(cnt[f][0])
			a = math.sqrt((end[0] - start[0])**2 + (end[1] - start[1])**2)
			b = math.sqrt((far[0] - start[0])**2 + (far[1] - start[1])**2)
			c = math.sqrt((end[0] - far[0])**2 + (end[1] - far[1])**2)
			angle = math.acos((b**2 + c**2 - a**2)/(2*b*c)) * 57
			if angle <= 90:
				count_defects += 1
				cv2.circle(crop_img, far, 1, [0,0,255], -1)
			#cv2.line(crop_img, start, end, [0,255,0], 2)
		return count_defects


	def within_two(self, defect, count):
		if count >= defect - 1 and count <= defect + 1:
		    return True
		return False
