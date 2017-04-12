import base64
import logging
import os
import uuid

from open_cv_handler import OpenCVHandler
from websocket_server import WebsocketServer

def on_client_connect(client, server):
    server.send_message(client, 'Welcome, Young One.')

def on_client_message(client, server, message):
    print 'Got image string.'

    # Process image data.
    decoded_image_string = base64.decodestring(message)

    # Give each image that comes in a unique universal id because each the
    # processes each client in a SEPARATE thread.
    uid = uuid.uuid4().hex
    image_path = 'image_%s' % (uid)

    # Write image to server.
    write_image_to_server(decoded_image_string, image_path)

    print 'Getting text translation.'

    opencv_handler = OpenCVHandler()
    # Get translation from OpenCV then play text audio
    text_trans = opencv_handler.get_text_translation_from_image(image_name, uid)
    opencv_handler.play_audio_translation_from_text(text_trans)

    print 'Sending back translation.'
    server.send_message(client, text_trans)

    print 'Removing written image from server.'
    remove_image_from_server(image_path)


def write_image_to_system(decoded_image_string, image_path):
    print 'Writing image to server.'
    f = open(image_path, 'wb')
    f.write(decoded_image_string)
    f.close()

def remove_image_from_server(image_path):
    print 'Removing written image from server.'
    os.remove(image_path)

def main():
    ip = raw_input('Enter ip address: ')

    print 'Starting the server.'
    server = WebsocketServer(8080, host=ip, loglevel=logging.INFO)

    server.set_fn_new_client(on_client_connect)
    server.set_fn_message_received(on_client_message)

    server.run_forever()

if __name__ == '__main__':
    main()
