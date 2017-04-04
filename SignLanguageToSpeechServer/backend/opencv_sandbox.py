import cv2
import numpy as np
import math

import win32com.client
import pyttsx

def get_text_translation_from_image(image_name):

    (version, _, _) = cv2.__version__.split('.')
    value = (35, 35)

    imA = cv2.imread('sign_a.jpg')
    grey = cv2.cvtColor(imA, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grey, value, 0)
    _, A_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

    imB = cv2.imread('sign_b.jpg')
    grey = cv2.cvtColor(imB, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grey, value, 0)
    _, B_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

    imC = cv2.imread('sign_c.jpg')
    grey = cv2.cvtColor(imC, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grey, value, 0)
    _, C_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

    imD = cv2.imread('sign_d.jpg')
    grey = cv2.cvtColor(imD, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grey, value, 0)
    _, D_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

    imE = cv2.imread('sign_e.jpg')
    grey = cv2.cvtColor(imE, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grey, value, 0)
    _, E_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

    imF = cv2.imread('sign_f.jpg')
    grey = cv2.cvtColor(imF, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(grey, value, 0)
    _, F_thresh = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

    if version is '3':
        _, A_contours, _ =  cv2.findContours(A_thresh.copy(), \
                   cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
        _, B_contours, _ =  cv2.findContours(B_thresh.copy(), \
                   cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
        _, C_contours, _ =  cv2.findContours(C_thresh.copy(), \
                   cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
        _, D_contours, _ =  cv2.findContours(D_thresh.copy(), \
                   cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
        _, E_contours, _ =  cv2.findContours(E_thresh.copy(), \
                   cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
        _, F_contours, _ =  cv2.findContours(F_thresh.copy(), \
                   cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

    elif version is '2':
        A_contours, _ = cv2.findContours(A_thresh.copy(),cv2.RETR_TREE, \
                   cv2.CHAIN_APPROX_NONE)
        B_contours, _ = cv2.findContours(B_thresh.copy(),cv2.RETR_TREE, \
                   cv2.CHAIN_APPROX_NONE)
        C_contours, _ = cv2.findContours(C_thresh.copy(),cv2.RETR_TREE, \
                   cv2.CHAIN_APPROX_NONE)
        D_contours, _ = cv2.findContours(D_thresh.copy(),cv2.RETR_TREE, \
                   cv2.CHAIN_APPROX_NONE)
        E_contours, _ = cv2.findContours(E_thresh.copy(),cv2.RETR_TREE, \
                   cv2.CHAIN_APPROX_NONE)

    #cv2.drawContours(im, A_contours, -1, (0,255,0), 3)
    # cv2.imshow("im",crop)
    img = image_name
    height, width, channels = img.shape
    cv2.rectangle(img,(width / 2, height / 3),(width / 5,0),(0,255,0),0)

    crop_img = img[0:(height / 3), (width / 5):(width / 2)]
    # cv2.imshow('im', crop_img)

    grey = cv2.cvtColor(crop_img, cv2.COLOR_BGR2GRAY)
    value = (35, 35)
    blurred = cv2.GaussianBlur(grey, value, 0)
    _, thresh1 = cv2.threshold(blurred, 127, 255, cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)

    if version is '3':
        image, contours, hierarchy = cv2.findContours(thresh1.copy(), \
               cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

    elif version is '2':
        contours, hierarchy = cv2.findContours(thresh1.copy(),cv2.RETR_TREE, \
               cv2.CHAIN_APPROX_NONE)

    #cv2.putText(thresh1,"Jason made this", (50,50), cv2.FONT_HERSHEY_SIMPLEX, 2, 2)

    cnt = max(contours, key = lambda x: cv2.contourArea(x))
    A_cnt = max(A_contours, key = lambda x: cv2.contourArea(x))
    B_cnt = max(B_contours, key = lambda x: cv2.contourArea(x))
    C_cnt = max(C_contours, key = lambda x: cv2.contourArea(x))
    D_cnt = max(D_contours, key = lambda x: cv2.contourArea(x))
    E_cnt = max(E_contours, key = lambda x: cv2.contourArea(x))
    F_cnt = max(F_contours, key = lambda x: cv2.contourArea(x))

    x,y,w,h = cv2.boundingRect(cnt)

    corrA = cv2.matchShapes(cnt,A_cnt,1,0.0)
    corrB = cv2.matchShapes(cnt,B_cnt,1,0.0)
    corrC = cv2.matchShapes(cnt,C_cnt,1,0.0)
    corrD = cv2.matchShapes(cnt,D_cnt,1,0.0)
    corrE = cv2.matchShapes(cnt,E_cnt,1,0.0)
    corrF = cv2.matchShapes(cnt,F_cnt,1,0.0)

    # corr_map = {
    # 	corrA : 'A',
    # 	corrB : 'B',
    # 	corrC : 'C',
    # 	corrD : 'D',
    # 	corrE : 'E',
    # 	corrF : 'F'
    # }
    #
    # for key in corr_map:
    # 	print '%s : %s' % (corr_map[key], key)
    # print ''
    #
    # corr_list = corr_map.keys()
    # corr_list.sort()
    # return corr_map[corr_list[0 ]]

    if corrA < 0.12:
        return 'A'

    elif corrB < 0.12:
        return 'B'

    elif corrC < 0.12:
         return 'C'

    elif corrD < 0.13:
         return 'D'

    elif corrE < 0.13:
         return 'E'

    elif corrF < 0.13:
         return 'F'

    else:
        return 'No Translation Found'

def main():
    webcam = cv2.VideoCapture(0)
    while True:
        ret, frame = webcam.read()

        height, width, channels = frame.shape
        cv2.rectangle(frame,(width / 2, height / 3),(width / 5,0),(0,255,0),0)

        cv2.imshow('Video', frame)
        cv2.waitKey(1)
        print get_text_translation_from_image(frame)

main()
