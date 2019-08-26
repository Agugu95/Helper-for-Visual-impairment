# Helper-for-Visual-impairment

## 시각장애인을 위한 보조 도우미 어플리케이션  
### 개발 환경  
#### Server-side  
- Python 
- Jquery 
- MicroSoft Azure B1S Instance  
- MicroSoft Azure Blob Storage  
- Window 10 / Mac / Linux Ubuntu Server 16.04(B1S)  
- Putty  
- Using API  
  > Azure Computer Vision API  
    Naver PAPAGO NMT Translater API  
    공공 데이터 포털 Open API (재난 예비 특보 조회, 대기오염 정보 조회)  

#### Client-side  
- Java  
- Android SDK 23 or later  
- Android Studio  
- Galaxy Tab 8.0  
- Retrofit2 / HttpUrlConnection  
- Using API  
  > Google Sppech API (TTS/STT)  
    Google Geocoder API  
    Android Location Manager  
  
## Flow  
![image](https://user-images.githubusercontent.com/38939634/63690856-af8d8a00-c848-11e9-9831-0e5cf9233a44.png)  

![image](https://user-images.githubusercontent.com/38939634/63691550-79510a00-c84a-11e9-940e-00438cd1095a.png)
완성본의 경우  
1. Click Button Detecking Voice and Captured Image
2. Saved Capture Image
3. Using Retrofit and POST transport image(multpart-form data)
4. 서버로 전송된 바이너리 이미지는 Restful API 구현을 위해 순차적으로 Blob 저장소에 저장  
5. 저장의 이미지 URL을 사용하여 Computer Vision API 구현  
6. CV는 오브젝트 검출 과정을 거쳐 결과를 EN JSON으로 반환  
7. EN JSON 형식 데이터를 Value값만 파싱  
8. Value 값을 PAPAGO API를 통해 구현  
9. 반환 된 ko String을 클라이언트로 전달  
10. 클라이언트는 받은 문자열을 speech API를 통해 음성으로 변환 후 재생  
> [Azure Computer Vision](https://azure.microsoft.com/ko-kr/services/cognitive-services/computer-vision/)  
  [Naver PAPAGO NMT Translater](https://developers.naver.com/docs/nmt/reference/)  
  

## 클라이언트 처리  
![image](https://user-images.githubusercontent.com/38939634/63691656-ca60fe00-c84a-11e9-927b-9def9f0d6378.png)  
android location manager를 이용해 알아낸 네트워크 상 위치(위도, 경도)를 Revers Geocoding을 통해   
문자열 주소로 변환 및 버튼 클릭에 따라 공공 데이터를 얻어왔음  
> [Google text-to-speech](https://cloud.google.com/text-to-speech/)  
  [Google speech-to-text](https://cloud.google.com/speech-to-text/)  
  [Google Geocoding](https://developers.google.com/maps/documentation/geocoding/intro)  
  [공공 데이터포털 대기오염 API](https://www.data.go.kr/dataset/15000581/openapi.do)  
  [공공 데이터포털 날씨조회 API](https://www.data.go.kr/dataset/15000099/openapi.do)  
  
### 네트워크 관련  
HttpUrlConnction과 Retrofit을 둘 다 사용해봤음  
Retrofit의 경우 자동적으로 Async Task를 통해 HTTP 커넥션을 생성함  
HttpUrlConnection의 경우 별개로 Async Task를 생성해주지 않으면  UI Thread Blocking이 일어남  
별도로 작업 스레드를 설정해줘야 백그라운드에서 HTTP 커넥션이 이루어짐  
