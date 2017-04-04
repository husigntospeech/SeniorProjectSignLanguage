#!/usr/bin/python
import base64
import io
import json

from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
from open_cv_handler import OpenCVHandler

class ServerHandler(BaseHTTPRequestHandler):
	def do_GET(self):
		self.send_response(200)
		self.send_header('Content-type','text/html')
		self.end_headers()
		self.wfile.write("<h1>Welcome.</h1>")

	def do_POST(self):
		# Processing HTTP POST request data
		content_len = int(self.headers.getheader('content-length', 0))
		post_body_json = self.rfile.read(content_len)
		post_body = json.loads(post_body_json)
		image_data = post_body.get('img_string_b64', 'No Image String')

		# Processing image data
		image_name = 'image.jpg'
		decoded_str = base64.decodestring(image_data)
		self.write_image_to_system(decoded_str, image_name)

		# Get translation from OpenCV then play text audio
		text_trans = OpenCVHandler.get_text_translation_from_image(image_name)
		OpenCVHandler.play_audio_translation_from_text(text_trans)

		# Responding to the POST requester.
		# text_trans = 'Translated'
		response = text_trans
		self.send_response(200)  # OK
		self.send_header('Content-type', 'text/html')
		self.end_headers()
		self.wfile.write(response)

	def write_image_to_system(self, decoded_str, image_name):
		print 'Writing image to server'
		f = open(image_name, 'wb')
		f.write(decoded_str)
		f.close()

def main():
	try:
		# 10.224.90.237
		ip_address = raw_input('Enter IP Address to host server: ')
		server = HTTPServer((ip_address, 8080), ServerHandler)
		print 'HTTPServer started'
		server.serve_forever()

	except KeyboardInterrupt:
		print 'server.socket.close()'
		server.socket.close()

if __name__ == '__main__':
	main()
