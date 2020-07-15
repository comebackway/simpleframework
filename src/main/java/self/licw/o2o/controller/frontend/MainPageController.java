package self.licw.o2o.controller.frontend;

import self.licw.o2o.entity.dto.MainPageInfoDto;
import self.licw.o2o.entity.dto.Result;
import self.licw.o2o.service.combine.HeadLineShopCategoryCombineService;
import self.licw.simpleframework.core.annotation.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class MainPageController {
    private HeadLineShopCategoryCombineService headLineShopCategoryCombineService;
    public Result<MainPageInfoDto> getMainPageInfo(HttpServletRequest request, HttpServletResponse response){
        return headLineShopCategoryCombineService.getMainPageInfo();
    }
}
