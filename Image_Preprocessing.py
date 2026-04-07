import sys
import argparse
import cv2

def preprocess_image(image_path, output_path):
    #Args: input_path path to orginal image, output_path path to save preprocessed image
    try: 
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE) # Read the image in grayscale
        if img is None:
            print(f"Error: Could not read image from {image_path}")
            sys.exit(1)
        
        resized_img = cv2.resize(img, (128, 128), interpolation=cv2.INTER_AREA) # Resize the image to 128x128
        success = cv2.imwrite(output_path, resized_img) # Save the preprocessed image
        if success:
            print(f"Successfully processed: {output_path}")
        else:
            print(f"Error: Could not write image to {output_path}")
    except Exception as e:
        print(f"Error processing {image_path}: {e}")
def main():
    parser = argparse.ArgumentParser(description='Preprocess images to 128x128 grayscale using OpenCV.')
    parser.add_argument('input_path', type=str, help='Path to the original image')
    parser.add_argument('output_path', type=str, help='Path to save the preprocessed image')
    
    args = parser.parse_args()
    preprocess_image(args.input_path, args.output_path)
if __name__ == "__main__":
    main()
