package com.example.gomplay.domain.survey.service;

import com.example.gomplay.domain.survey.dto.ReportResponse;
import com.example.gomplay.domain.survey.entity.UserSurvey;
import com.example.gomplay.domain.survey.entity.UserSurveyExercise;
import com.example.gomplay.domain.survey.repository.UserSurveyExerciseRepository;
import com.example.gomplay.domain.survey.repository.UserSurveyRepository;
import com.example.gomplay.domain.user.entity.UserProfile;
import com.example.gomplay.domain.user.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserSurveyRepository userSurveyRepository;
    private final UserSurveyExerciseRepository userSurveyExerciseRepository;
    private final UserProfileRepository userProfileRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=";

    @Transactional(readOnly = true)
    public ReportResponse generateReport(Long userId) {
        UserProfile userProfile = userProfileRepository.findByAuthUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        UserSurvey survey = userSurveyRepository.findByUserProfile_Id(userProfile.getId())
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다."));

        List<UserSurveyExercise> exercises = userSurveyExerciseRepository
                .findByUserProfile_Id(userProfile.getId());

        String prompt = buildPrompt(survey, exercises);
        String jsonResult = callGemini(prompt);

        return parseReport(userProfile.getId(), survey, exercises, jsonResult);
    }

    private String mapSummary(String personalityType, String intensityType, String purposeType) {
        String key = personalityType + "+" + intensityType + "+" + purposeType;
        return switch (key) {
            case "독립형+여유형+힐링 추구"  -> "혼자 조용히, 느긋하게 몸과 마음을 충전하는 스타일이에요!";
            case "독립형+여유형+관계 중심"  -> "같이 있어도 각자 페이스로, 가볍게 땀 흘리며 친해지는 스타일이에요!";
            case "독립형+여유형+실력 향상"  -> "혼자 묵묵히, 부담 없이 조금씩 실력을 쌓아가는 스타일이에요!";
            case "독립형+여유형+건강 관리"  -> "무리 없이 꾸준하게, 내 몸을 챙기는 걸 즐기는 스타일이에요!";
            case "독립형+균형형+힐링 추구"  -> "혼자 적당히 땀 흘리며 스트레스를 날려버리는 스타일이에요!";
            case "독립형+균형형+관계 중심"  -> "각자 운동하면서도 자연스럽게 유대감을 쌓아가는 스타일이에요!";
            case "독립형+균형형+실력 향상"  -> "혼자 집중하면서 균형 잡힌 성장을 추구하는 스타일이에요!";
            case "독립형+균형형+건강 관리"  -> "적당한 강도로 꾸준히, 건강한 루틴을 만들어가는 스타일이에요!";
            case "독립형+집중형+힐링 추구"  -> "혼자 몰입하며 운동으로 머릿속을 완전히 비워버리는 스타일이에요!";
            case "독립형+집중형+관계 중심"  -> "말 없이 옆에서 함께 집중하는 것만으로도 충분한 스타일이에요!";
            case "독립형+집중형+실력 향상"  -> "혼자 깊이 파고들며 실력을 끌어올리는 걸 즐기는 스타일이에요!";
            case "독립형+집중형+건강 관리"  -> "제대로 집중해서 몸을 단단하게 만들어가는 스타일이에요!";
            case "독립형+도전형+힐링 추구"  -> "혼자 한계까지 밀어붙이며 스트레스를 통쾌하게 날리는 스타일이에요!";
            case "독립형+도전형+관계 중심"  -> "말 없이 함께 한계를 넘으며 진짜 유대감을 만드는 스타일이에요!";
            case "독립형+도전형+실력 향상"  -> "혼자서 끝까지 밀어붙이며 한 단계씩 성장하는 스타일이에요!";
            case "독립형+도전형+건강 관리"  -> "한계까지 도전하며 몸을 철저하게 관리하는 스타일이에요!";
            case "소통형+여유형+힐링 추구"  -> "수다 떨며 가볍게 땀 흘리는 게 최고의 힐링인 스타일이에요!";
            case "소통형+여유형+관계 중심"  -> "운동보다 대화가 더 즐거운, 친목이 먼저인 스타일이에요!";
            case "소통형+여유형+실력 향상"  -> "같이 이야기하면서 느긋하게 실력도 키워가는 스타일이에요!";
            case "소통형+여유형+건강 관리"  -> "편하게 대화하며 건강도 챙기는 일석이조 스타일이에요!";
            case "소통형+균형형+힐링 추구"  -> "같이 운동하며 떠들다 보면 스트레스가 싹 날아가는 스타일이에요!";
            case "소통형+균형형+관계 중심"  -> "적당히 운동하면서 사람과의 시간을 제일 소중히 여기는 스타일이에요!";
            case "소통형+균형형+실력 향상"  -> "함께 으쌰으쌰하며 균형 있게 실력을 키워가는 스타일이에요!";
            case "소통형+균형형+건강 관리"  -> "대화하며 적당히 움직이고, 건강하게 일상을 채워가는 스타일이에요!";
            case "소통형+집중형+힐링 추구"  -> "같이 제대로 땀 흘리고 나서 후련함을 나누는 스타일이에요!";
            case "소통형+집중형+관계 중심"  -> "함께 열심히 하면서 더 깊이 친해지는 걸 즐기는 스타일이에요!";
            case "소통형+집중형+실력 향상"  -> "서로 자극 주고받으며 제대로 성장하는 걸 즐기는 스타일이에요!";
            case "소통형+집중형+건강 관리"  -> "같이 집중해서 운동하며 서로의 건강을 응원하는 스타일이에요!";
            case "소통형+도전형+힐링 추구"  -> "같이 한계까지 불태우고 나서 후련함을 함께 나누는 스타일이에요!";
            case "소통형+도전형+관계 중심"  -> "함께 극한까지 도전하며 끈끈한 전우애를 쌓는 스타일이에요!";
            case "소통형+도전형+실력 향상"  -> "서로 밀어주며 한계를 돌파하는 성장 지향 스타일이에요!";
            case "소통형+도전형+건강 관리"  -> "함께 한계까지 도전하며 몸과 관계를 동시에 단련하는 스타일이에요!";
            default -> "나만의 운동 스타일을 만들어가는 중이에요!";
        };
    }

    private String mapPersonalityType(UserSurvey.PartnerStyle partnerStyle) {
        return switch (partnerStyle) {
            case 각자 -> "독립형";
            case 같이 -> "소통형";
        };
    }

    private String mapIntensityType(UserSurvey.ExerciseIntensity intensity) {
        return switch (intensity) {
            case 가볍게 -> "여유형";
            case 적당히 -> "균형형";
            case 제대로 -> "집중형";
            case 한계까지 -> "도전형";
        };
    }

    private String mapPurposeType(UserSurvey.ExerciseReason reason) {
        return switch (reason) {
            case 스트레스 -> "힐링 추구";
            case 친해지려고 -> "관계 중심";
            case 경쟁 -> "실력 향상";
            case 체력 -> "건강 관리";
        };
    }

    private String buildPrompt(UserSurvey survey, List<UserSurveyExercise> exercises) {
        String exerciseList = exercises.stream()
                .map(e -> e.getExerciseType().name())
                .collect(Collectors.joining(", "));

        return String.format("""
                당신은 운동 전문 코치입니다. 아래 사용자의 운동 설문 데이터를 바탕으로 분석 레포트를 작성해주세요.
                
                [사용자 설문 데이터]
                - 파트너 스타일: %s (각자 = 혼자서 독립적으로 운동, 같이 = 파트너와 함께 운동)
                - 운동 강도: %s (가볍게/적당히/제대로/한계까지)
                - 운동 이유: %s (스트레스 해소/친해지려고/경쟁/체력 향상)
                - 관심 운동 종목: %s
                
                아래 JSON 형식으로만 응답해주세요. 다른 텍스트는 절대 포함하지 마세요.
                {
                  "recommendedExercises": ["종목1", "종목2", "종목3"],
                  "partnerStyleDescription": "파트너 스타일 설명 (2~3문장)",
                  "exerciseMoodDescription": "운동 분위기 설명 (2~3문장)"
                }
                
                조건:
                - recommendedExercises는 관심 종목을 참고하되, 설문 데이터 전체를 고려해 최적의 3가지 종목을 추천하세요.
                - partnerStyleDescription은 이 유저에게 잘 맞는 파트너 유형을 구체적으로 설명하세요.
                - exerciseMoodDescription은 이 유저에게 맞는 운동 분위기와 환경을 설명하세요.
                """,
                survey.getPartnerStyle().name(),
                survey.getExerciseIntensity().name(),
                survey.getExerciseReason().name(),
                exerciseList
        );
    }

    private String callGemini(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_URL + geminiApiKey, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String raw = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Gemini가 ```json ... ``` 블록으로 감쌀 수 있어서 제거
            return raw.replaceAll("(?s)```json\\s*|```", "").trim();
        } catch (Exception e) {
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage());
        }
    }

    private ReportResponse parseReport(Long userId, UserSurvey survey, List<UserSurveyExercise> exercises, String jsonResult) {
        try {
            JsonNode node = objectMapper.readTree(jsonResult);

            List<String> recommendedExercises = objectMapper.convertValue(
                    node.path("recommendedExercises"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            String personalityType = mapPersonalityType(survey.getPartnerStyle());
            String intensityType   = mapIntensityType(survey.getExerciseIntensity());
            String purposeType     = mapPurposeType(survey.getExerciseReason());

            List<String> exerciseTypes = exercises.stream()
                    .map(e -> e.getExerciseType().name())
                    .collect(Collectors.toList());

            return ReportResponse.builder()
                    .userId(userId)
                    .personalityType(personalityType)
                    .intensityType(intensityType)
                    .purposeType(purposeType)
                    .summary(mapSummary(personalityType, intensityType, purposeType))
                    .exerciseTypes(exerciseTypes)
                    .recommendedExercises(recommendedExercises)
                    .partnerStyleDescription(node.path("partnerStyleDescription").asText())
                    .exerciseMoodDescription(node.path("exerciseMoodDescription").asText())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("레포트 파싱 실패: " + e.getMessage());
        }
    }
}