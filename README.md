## 스마트워치와 AI를 활용한 낙상 감지 및 혈당 관리 서비스 Dialog
![ezgif-3-2446187e13](https://github.com/user-attachments/assets/7f6bb330-65dd-41ef-8882-7edb91489c0e)

## 사용 기술
> Kotlin <br/>
> Android Studio

## 프로젝트 실행 방법
깃 레포지토리 클론
```bash
git clone https://github.com/epilog-swu/Front.git
```
안드로이드 스튜디오에서 get From VCS -> 리포지토리 주소 입력

## 주요 기능

### 모바일

모바일에서는 환자의 식사 전후 시간대에 따라 혈당, 혈압, 몸무게 및 체지방율, 운동, 기분 항목을 입력할 수 있습니다.  <br/>
직관적인 UI를 통해 캘린더 페이지에서는 날짜별 일지 개수와 목록을 쉽게 파악하고, 조회할 수 있습니다.   <br/>
메인 그래프 페이지에서는 선택한 날짜에 대한 '일별 혈당 그래프' 와 '월별 몸무게 및 체지방율 그래프' 를 제공합니다. <br/>

작성된 기록은 보고서 형태로도  확인 가능하며, <br/>
서면 제출에 용이하도록 선택한 기간의 기록을 PDF로 변환하는 기능을 제공합니다. <br/>

복약 체크리스트에서 복약하는 약정보를 등록하고, 이를 체크할 수 있습니다. <br/>

### 워치

워치에서 낙상이 감지되면 사용자에게 modal이 표시되며, 이를 클릭하지 않으면 사전에 등록된 보호자 전화번호로 긴급 sms을 발송합니다. <br/>
긴급 sms 알림에는 낙상이 발생된 주소와 지도 이미지가 첨부되며, 워치에서 감지된 낙상은 모바일에도 자동으로 기록됩니다. <br/>

워치에서도 날짜를 클릭하여 그 날의 혈당 변동 추이를 그래프로 확인할 수 있고, 혈당 추가도 할 수 있습니다.<br/>
식사 및 복약 체크리스트 체크가 가능하며, 워치의 햅틱 인터페이스를 활용하여 복약 시간과 식사시간에 알림 기능을 제공합니다.<br/>


## 시스템 아키텍처

<img src="https://github.com/user-attachments/assets/d3b6b476-8af9-4618-a8ee-5f9dec852432" width ="850" height="1000"/>



## 플로우 차트

<img src="https://github.com/user-attachments/assets/ec1dbeeb-482d-403c-ba8e-e5f5e1bce220" width="1000" height="500"/>


<img src="https://github.com/user-attachments/assets/3bcd5154-7de2-4f1b-9eb4-74e85397209e" width="900" height="450"/>

## 디렉토리 구조

### 앱모듈
```
📁 App
├── manifests
│    └── AndroidManifest.xml
└── kotlin+java
│    └── com.epi.epilog
│        ├── api
│        ├── diary
│        ├── main
│        ├── meal
│        ├── medicine
│        ├── my
│        ├── signup
│        └── ui.theme
└── java
└── res
```
### Wear 모듈
```
📁 Wear
├── manifests
│    └── AndroidManifest.xml
└── kotlin+java
│    └── api
│        └── RetrofitService.kt
│    └── com.epi.epilog.presentation
│        ├── blood
│        ├── fall
│        ├── main
│        ├── meal
│        ├── medicine
│        └── theme
└── java
└── res
```


## 팀원
<table>
  <tr>
    <td>양수빈</td>
    <td>박현아</td>
    <td>신서영</td>
  </tr>
  <tr>
    <td>Server/AI Developer</td>
    <td>Android Developer</td>
    <td>Android Developer</td>
  </tr>
</table>
