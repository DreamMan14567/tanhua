import com.tanhua.domain.db.BasePojo;
import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */
@Data
public class Result extends BasePojo {
    // 结果主键
    private Long id;
    // 试卷ID
    private Long pid;
    // 结论ID
    private Long conclusionId;
    // 用于指向维度ID
    private Long dimensionId;
    // 用户ID
    private Long userId;
}
