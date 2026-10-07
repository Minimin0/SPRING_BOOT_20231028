-- 회원가입 화면에서 관리자 계정을 먼저 생성한 뒤 실행한다.
SELECT id, username, name, role FROM member ORDER BY id;

SET @admin_username = 'admin';

UPDATE member
SET role = 'ADMIN'
WHERE username = @admin_username;

SELECT id, username, name, role FROM member ORDER BY id;
