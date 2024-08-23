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
