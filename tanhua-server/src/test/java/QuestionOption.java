import com.tanhua.domain.db.BasePojo;
import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */
@Data
public class QuestionOption extends BasePojo {
    // 问题选项主键
    private Long id;
    // 选项全程
    private String optionName;
    // 所属问题ID
    private Long questionId;
    // 选项分数
    private Long score;
}
