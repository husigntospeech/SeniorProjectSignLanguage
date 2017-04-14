from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
from SocketServer import ThreadingMixIn
import threading
import os

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""
    pass

class MyHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        request = self

        path = request.path[1:]
        request.send_response(200)

        if request.path == '/':
            path = 'home.html'
            request.send_header('Content-type', 'text/html')
        elif '.css' in request.path:
            request.send_header('Content-type', 'text/css')
        elif '.js' in request.path:
            request.send_header('Content-type', 'text/javavscript')
        else:
            print 'Unknown request for: %s' % (request.path)
            request.send_response(404)
            return

        with open(path) as f:
            f_content = f.read()
        f.close()

        request.end_headers()
        request.wfile.write(f_content)

        return

    def do_POST(self):
        pass

def main():
    ip = raw_input('Enter ip address: ')
    try:
        server = ThreadedHTTPServer((ip, 8000), MyHandler)
        print 'HTTPServer started'
        server.serve_forever()

    except KeyboardInterrupt:
        print 'server.socket.close()'
        server.socket.close()

if __name__ == '__main__':
    main()
