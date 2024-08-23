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
