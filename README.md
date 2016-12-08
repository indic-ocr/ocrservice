### Host and run OCR as a service within your organisation or community. 

OCR service is dependent on following:

1. [Java](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html)
2. [Maven](https://maven.apache.org/)
3. [Olena](https://indic-ocr.github.io/olena/)
4. [Tesseract](https://github.com/tesseract-ocr/tesseract)
5. [Tessdata (for Indic scripts support)](https://indic-ocr.github.io/tessdata/)


Checkout the code 

    git clone https://github.com/indic-ocr/ocrservice.git 

To compile and start the server use following command

    mvn package  && java -jar target/IndicOCR-jar-with-dependencies.jar <path_to_olena>/scribo/src/content_in_doc

On my local system it looks like this

    mvn package  && java -jar target/IndicOCR-jar-with-dependencies.jar ~/ocr/olena/olena/scribo/src/content_in_doc

The server start on port 8081 and exposes 3 webservice APIs

* _/ocr_ which converts and image to an ODT file
* _/india_ which converts an image to text using the scribo engine
* _/indiastring_ which converts an image (uploaded, http url or data url)  using tesseract or scribo and can also do invert or binarization of image before passing it to OCR engine


An experimental server is available on <http://35.164.84.230:8081/>. All images are removed from the server at least once a day and **they are not stored**

####Usage Examples

*_/ocr_*    
    
    curl   -F "dpi=300"   -F "lang=eng"   -F "myfile=@<path_to_image_file>" http://35.164.84.230:8081/ocr

*_/india_*

    curl   -F "tolang=eng"   -F "sourcelang=pan"   -F "myfile=@<path_to_binarized_image>" http://35.164.84.230:8081/india
    
*_/indiastring_*

    curl -H "Content-Type: application/json" -X POST -d '{"filePath":"<http url or data url >", "sourcelang":"pan","tolang":"eng","operation":"invert","engine":"tesseract"}' http://35.164.84.230:8081/indiastring

    
* Allowed operations are _normal, invert or binarize_
* Allowed values for engine are _tesseract or scribo_
* All language parameters need to be 3 letter codes ( _eg: eng for English, tam for Tamil_)
 


### Authors and Contributors
@rkvsraman

### Help
Please join the project and help by code contributions or by reporting bugs. 
