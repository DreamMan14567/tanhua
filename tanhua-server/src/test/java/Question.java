import com.tanhua.domain.db.BasePojo;
import lombok.Data;

/**
 * @author user_Chubby
 * @date 2021/5/23
 * @Description
 */
@Data
public class Question extends BasePojo {
    // 问题ID
    private Long id;
    // 问题全程
    private String name;
    // 问题类型
    private Integer typeId;
    //所属试卷ID
    private Long pageId;
}
