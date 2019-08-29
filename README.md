# Helper-for-Visual-impairment

## 시각장애인을 위한 보조 도우미 어플리케이션  
### 개발 환경  
#### Server-side  
- Python / Flask
- Jquery / Beutiful Soap   
- MicroSoft Azure B1S Instance  
- MicroSoft Azure Blob Storage  
- Window 10 / Mac / Linux Ubuntu Server 18.04.01 LTS(B1S)  
- Putty(.ppk 공개 퍼블릭 키)  
- Using API  
  > Azure Computer Vision API  
    Naver PAPAGO NMT Translater API  
    공공 데이터 포털 Open API (재난 예비 특보 조회, 대기오염 정보 조회)  

#### Client-side  
- Java  
- Android SDK 23 or later  
- Android Studio  
- Galaxy Tab 8.0  
- Retrofit2 / HttpUrlConnection / Gson  
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
문자열 주소로 변환 및 버튼 클릭에 따라 데이터를 전송  
> [Google text-to-speech](https://cloud.google.com/text-to-speech/)  
  [Google speech-to-text](https://cloud.google.com/speech-to-text/)  
  [Google Geocoding](https://developers.google.com/maps/documentation/geocoding/intro)  
  
![image](https://user-images.githubusercontent.com/38939634/63852941-6f5b1280-c9d4-11e9-830b-51d21a62b9d5.png)  
서버에서 오는 정확한 파라미터 갯수를 모르기때문에 메소드 파라미터를 배열로 받는 ... 문법을 사용했음  

![image](https://user-images.githubusercontent.com/38939634/63853083-c52fba80-c9d4-11e9-8e1c-860ec1c84d8b.png)  
문자열로 받아온 위치정보에서 필요한 데이터만을 추출  

  
  
### 네트워크 관련  
HttpUrlConnction과 Retrofit을 둘 다 사용해봤음  
Retrofit의 경우 자동적으로 Async Task를 통해 HTTP 커넥션을 생성함  
HttpUrlConnection의 경우 별개로 Async Task를 생성해주지 않으면  UI Thread Blocking이 일어남  
별도로 작업 스레드를 설정해줘야 백그라운드에서 HTTP 커넥션이 이루어짐  

### 서버 처리  
![image](https://user-images.githubusercontent.com/38939634/63853229-1c358f80-c9d5-11e9-8498-971f35ba281d.png)  
API Key를 METADATA로 별도 관리하고, 라우트를 통해 요청을 처리  

![image](https://user-images.githubusercontent.com/38939634/63853370-7f272680-c9d5-11e9-981f-728d9bd60531.png)  
- Computer Vision API 사용과정  
  객체 인식에 대한 여러 설정을 지정하고, 결과값을 JSON으로 리턴받아 파싱  

![image](https://user-images.githubusercontent.com/38939634/63853473-bf86a480-c9d5-11e9-92d7-363cc272c31a.png)  
- Naver PAPAGO NMT 사용과정  
  문자는 UTF-8 인코딩 방식을 사용, Request Content Header는 x-www-form-urlencoded 형식  
  > x-www-form-urlencoded  
    페이로드가 적을 때(담아야하는 정보) 사용  



## 프로젝트에 관한 아쉬운 점  
- 문서화가 제대로 되어있지 않던 점  
  이전 프로젝트를 교훈삼아 분업화를 목표로 삼았었지만 그것뿐이었다는게 문제였음  
  각 파트를 나누긴 했으나 문서화의 부재로 서로 부족한 부분에 대한 인계도 어려웠고  
  특히나 API 사용과 코드 구현 부분에서 직접 찾아보며 알려줘야하는 문제가 있었음  
  또한 이후 코드를 봤을 때 API부터 다시 찾아보는 불상사가 발생함  
- 버전 관리의 미비   
  버전관리의 중요성은 알고 있었으나 실제로 4명이 한 공간에서 작업을 하는지라 별 생각이 없었음  
  그날 그날 수정한 코드는 그냥 보여주고 시간써서 수정하면 되니까  
  하지만 나중에 코드를 병합할 때 상당히 큰 문제가 생겼는데 원본 소스코드의 진위였음  
  병합해서 테스트만 해본 채로 실수로 원본 소스코드를 로컬에서 잃어버리거나 변경해버린바람에   
  완전한 완성본의 행방은 오리무중  
  
  기타 발생한 문제는 또 .. 서버 테스팅 관련해서 자동화 시스템이 없던 것
  매번 서버 테스팅에는 서버를 껐다가 켜야했다 
  
