import cv2
import os
from pyzbar import pyzbar

barcode_info = None


def read_barcodes(frame):
    barcodes = pyzbar.decode(frame)
    for barcode in barcodes:
        x, y, w, h = barcode.rect

        barcode_info = barcode.data.decode('utf-8')
        cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)
        with open("barcode_result.txt", mode='w') as file:
            file.write(barcode_info)
        print(barcode_info)
    return frame


def main():
    camera = cv2.VideoCapture(0)
    ret, frame = camera.read()

    while ret:
        ret, frame = camera.read()
        frame = read_barcodes(frame)
        cv2.imshow('QR Code Scanner', frame)
        if os.path.isfile('barcode_result.txt'):
            break
        else:
            continue

    camera.release()
    cv2.destroyAllWindows()


if __name__ == '__main__':
    if os.path.isfile('barcode_result.txt'):
        os.remove('barcode_result.txt')
    main()
