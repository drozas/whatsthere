from flask import Flask, request, jsonify
from werkzeug import secure_filename
import pexpect, sys, os
import threading
import json

child = pexpect.spawn('./darknet detect cfg/yolov3.cfg yolov3.weights')
child.timeout = 120
child.expect("Enter Image Path: ", timeout=None)

UPLOAD_FOLDER = '/tmp/flask'
os.system("mkdir " + UPLOAD_FOLDER)
ALLOWED_EXTENSIONS = set(['jpg'])

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
lock = threading.Lock()

def allowed_file(filename): return '.' in filename and filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

@app.route('/', methods=['GET'])
def test():
    print("GET request")
    return jsonify({'ok':True})

@app.route('/recon', methods=['POST'])
def recon():
        file = request.files['file']
        print(file.filename)
        if file and allowed_file(file.filename) and lock.acquire(False):
                filename = secure_filename(file.filename)
                abs_filename = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                file.save(abs_filename)
                child.sendline(abs_filename)
                child.expect("seconds.\r\n")
                resultList = []
                loop = True
                while loop :
                    child.expect(["%\r\n", "Enter Image Path:"])
                    classinfo = child.before.decode('utf8')
                    print(classinfo)
                    if( "Enter" in classinfo or classinfo == '') :
                        print("end")
                        loop = False
                    else :
                        c, p = classinfo.split(':')[0], float(classinfo.split(':')[1])
                        resultList.append({"class": c, "p": p})
                lock.release()
                return '{"status" : "ok", "result": ' + json.dumps(resultList) + '}'
        else:
                return '{"status" : "failed", "reason" : "Picture format is not allowed"}'

if __name__ == "__main__":
        app.run(debug=False, host='0.0.0.0', port=5500)