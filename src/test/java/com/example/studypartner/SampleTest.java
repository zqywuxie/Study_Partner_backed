package com.example.studypartner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studypartner.domain.entity.*;
import com.example.studypartner.domain.vo.TeamUserVO;
import com.example.studypartner.domain.vo.UserVO;
import com.example.studypartner.mapper.UserMapper;
import com.example.studypartner.service.*;
import com.example.studypartner.service.impl.QiniuCloudUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class SampleTest {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private UserService userService;

	@Autowired
	private TeamService teamService;

	@Resource
	private ChatService chatService;

	@Resource
	private OssService ossService;

	@Resource
	private QiniuCloud qiniuCloud;

	@Resource
	private BlogService blogService;

	@Resource
	private BloglikeService bloglikeService;

	@Resource
	private CommentsService commentsService;
	@Resource
	private CommentLikeService commentLikeService;


	//    @Test
//    public void testSelect() {
//        userService.Register("456789","123456","123456");
//    }


	@Test
	public void getBlogAndCommentsLikeCount() {
		Long userId = 3L;
		List<Long> blogIds = blogService.lambdaQuery()
				.eq(Blog::getUserId, userId)
				.list()
				.stream()
				.map(Blog::getId)
				.collect(Collectors.toList());
		List<Long> commentsIds = commentsService.lambdaQuery()
				.eq(Comments::getUserId, userId)
				.list()
				.stream()
				.map(Comments::getId)
				.collect(Collectors.toList());

		if (blogIds.isEmpty() && commentsIds.isEmpty()) {
			// Handle the case when the list is empty (e.g., return a default value or throw an exception)
			return;
		}

		LambdaQueryWrapper<BlogLike> blogLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		blogLikeLambdaQueryWrapper.in(BlogLike::getBlogId, blogIds);


		LambdaQueryWrapper<CommentLike> commentLikeLambdaQueryWrapper = new LambdaQueryWrapper<>();
		commentLikeLambdaQueryWrapper.in(CommentLike::getCommentId,
				blogIds);
		long totalCount = commentLikeService.count(commentLikeLambdaQueryWrapper) + bloglikeService.count(blogLikeLambdaQueryWrapper);
		System.out.println(totalCount);
	}

	@Test
	public void getCommentLikeCount() {
		LambdaQueryWrapper<Chat> chatLambdaQueryWrapper = new LambdaQueryWrapper<>();
		chatLambdaQueryWrapper.select(Chat::getToId).eq(Chat::getFromId, 3L).groupBy(Chat::getToId);
		List<Long> collect = chatService.list(chatLambdaQueryWrapper).stream().map(Chat::getToId).collect(Collectors.toList());
		LambdaQueryWrapper<User> userVOLambdaQueryWrapper = new LambdaQueryWrapper<>();
		userVOLambdaQueryWrapper.in(User::getId, collect);
		List<User> list = userService.list(userVOLambdaQueryWrapper);
		System.out.println(list);
	}

	@Test
	public void test() throws IOException {
		QiniuCloudUtil qiniuCloudUtil1 = new QiniuCloudUtil();
		File folder = new File("E:\\picture\\5339.zip");//文件夹路径
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < 1; i++) {
			if (listOfFiles[i].isFile()) {
				qiniuCloud.upload(listOfFiles[i]);
//                FileItem fileItem = createFileItem(listOfFiles[i],listOfFiles[i].getName());
//                CommonsMultipartFile commonsMultipartFile = new CommonsMultipartFile(fileItem);
//                ossService.uploadFileAvatar(commonsMultipartFile);
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("Directory " + listOfFiles[i].getName());
			}
		}
	}

	private FileItem createFileItem(File file, String fieldName) {
		FileItemFactory factory = new DiskFileItemFactory(16, null);
		FileItem item = factory.createItem(fieldName, "text/plain", true, file.getName());
		int bytesRead = 0;
		byte[] buffer = new byte[8192];
		try {
			FileInputStream fis = new FileInputStream(file);
			OutputStream os = item.getOutputStream();
			while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return item;
	}

//    @Test
//    private List<TeamUserVO> search () {
//        teamService.listTeams()
//    }

	private Integer code;

}

class INNER {
	private int n1 = 100;
	private static final int n2 = 10;

	private void m1() {
	}

	public void m2() {//成员方法
		class inner01 { //局部内部类
			private static final int n3 = 10;

			public void f1() {
				System.out.println(n1);
				m1();
			}
		}
	}
}
