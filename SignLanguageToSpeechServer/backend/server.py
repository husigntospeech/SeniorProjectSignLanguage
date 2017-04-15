import base64
import logging
import os
import uuid
import shutil

from open_cv_handler import OpenCVHandler
from lib.websocket_server import WebsocketServer

TEMP_FOLDER_PATH = 'temp'

def on_client_message(client, server, message):
    print 'Got message.'

    # If the message has a space in the 2nd position then the server will
    # interpret the message as a request to move the image with the given
    # uid to the appropriate image folder.
    # Otherwise, the server will treat the message a Base64 encoded string and
    # will proceed to try to decode it.
    if message[1] != ' ':
        # Process image data.
        decoded_image_string = base64.decodestring(message)

        # Give each image that comes in a unique universal id because each
        # client gets serviced by the server in a SEPARATE thread.
        uid = uuid.uuid4().hex
        image_path = '%s/image_%s' % (TEMP_FOLDER_PATH, uid)

        # Write image to server.
        write_image_to_server(decoded_image_string, image_path)

        print 'Getting text translation.'

        cv_handler = OpenCVHandler()

        # Get translation from OpenCV then play text audio
        text_trans = cv_handler.get_text_translation_from_image(image_path, uid)
        cv_handler.play_audio_translation_from_text(text_trans)

        print 'Sending back translation.'
        server.send_message(client, '%s %s' % (text_trans, uid))

        # Server will clean up after itself and clean up after the
        # OpenCV handler.
        remove_image_from_server(image_path)
    else:
        corrected_translation = message[0]
        uid = message[2:] # image uid
        if corrected_translation != '!':
            moveImageToCorrectLocation(corrected_translation, uid)
        else:
            victim_path = '%s/cropped_image_%s.jpg' % (TEMP_FOLDER_PATH, uid)
            remove_image_from_server(victim_path)

    print 'Done.\n\n'

def write_image_to_server(decoded_image_string, image_path):
    print 'Writing image to server.'
    f = open(image_path, 'wb')
    f.write(decoded_image_string)
    f.close()

def remove_image_from_server(image_path):
    print 'Removing written image from server.'
    os.remove(image_path)

def moveImageToCorrectLocation(translation, uid):
    print 'Moving cropped image to correct location.'
    image_name = 'cropped_image_%s.jpg' % (uid)
    new_folder = 'sign_%s' % (translation)

    image_path = '%s/%s' % (TEMP_FOLDER_PATH, image_name)
    new_path = '%s/%s' % (new_folder, image_name)
    shutil.move(image_path, new_path)

def main():
    ip = raw_input('Enter ip address: ')

    print 'Starting the server.'
    server = WebsocketServer(8080, host=ip, loglevel=logging.INFO)
    server.set_fn_message_received(on_client_message)
    server.run_forever()

if __name__ == '__main__':
    main()
