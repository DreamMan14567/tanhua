import com.tanhua.domain.db.BasePojo;
import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */

@Data
public class Answer extends BasePojo {
    // 回答主键
    private Long id;
    // 问题ID
    private Long questionId;
    // 选项ID
    private Integer optionId;
    // 回答用户ID
    private Long userId;
}
