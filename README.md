###框架说明


exit-web-framework是对常用的java web开发封装实用功能来提高开发效率的底层框架。exit-web-framework基于Spring 3做核心框架、Hibernate4或spring data jpa做持久化框架,用spring mvc 框架对mvc做管理。使用到的新功能有spring缓存工厂、apeche shiro安全框架、spring mvc 3,spring data jpa等主要流行技术, 该项目分为两个部分做底层的封装，和带两个项目功能演示例子。

[相关帮助文档](https://github.com/exitsoft/exit-web-framework/wiki)

[更新日志](https://github.com/exitsoft/exit-web-framework/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)

#### 初始化工作:

***

##### 配置maven

1. 下载[maven](http://maven.apache.org/download.html)
1. 解压maven-[版本]-bin.zip到你想安装的位置
1. 设置maven系统环境变量，M2_HOME=[maven安装目录位置]
1. 添加maven bin目录至系统环境变量PATH中， %M2_HOME%\bin
1. 确认Maven的安装：cmd > mvn -version

##### 安装exit-web-framework到maven中

1. 使用git或者svn下载exit-web-framework

***
	git地址:git://github.com/exitsoft/exit-web-framework.git
	svn地址:https://github.com/exitsoft/exit-web-framework.git
***

1. 点击根目录下的bin/install.bat文件进行安装,当看见该信息时表示安装成功:

***
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 1.875s
	[INFO] Finished at: Fri Jan 04 11:08:01 CST 2013
	[INFO] Final Memory: 6M/16M
	[INFO] ------------------------------------------------------------------------
***
该安装过程会将exit-web-framework中的exit-common和exit-orm生成jar包放入maven的.m2文件夹中,当然,这两个框架使用到的依赖jar包也会下载到.m2文件夹中.


##### exitsoft common 简单说明

该jar包是对基本的常用工具类的一些简单封装。如泛型，反射，配置文件等工具类的封装。

##### exitsoft orm 简单说明

该jar包是对持久化层的框架封装，目前只对Hibernate4和spring data jpa的CURD和辅助查询功能封装。

##### 项目功能演示例子

在文件夹的shorcase里有一个vcs-admin项目和vcs-admin-jpa项目。该两个项目是对以上两个框架(exit-common和exit-orm)和其他技术的整合做的例子，vcs-admin使用的是Hibernate 4 做持久化框架,而vcs-admin-jpa使用的是spring data jpa做持久化框架,通过该两个例子使用maven做了一个archetype基础模板。可以通过该archetype来生成一个新的项目。

该两个项目使用了mysql数据库,在vcs-admin或vcs-admin-jpa文件夹中有一个database的文件夹里面有对应该项目使用的数据。导入之后可以通过
vcs-admin或vcs-admin-jpa项目文件夹中的bin文件夹下的jetty.bat文件运行项目，也可以用eclipse.bat生成项目导入到开发工具中在运行。
该工程下有一个基于jeety运行的java文件org.exitsoft.showcase.vcsadmin.test.LaunchJetty.你也可以通过该文件运行整个项目.

##### archetype基础模板使用说明
1. 点击根目录的install.bat进行初始化(如果未点击安装时)
1. 点击archetype-generate.bat生成你的项目

##### 导入eclipse或者myeclipse
在根目录下的bin目录有一个eclipse.bat和myeclipse.bat，点击eclipse.bat/myeclipse.bat会生成project，看见以下信息表示生成成功，可以直接导入eclipse/myclipse

***
	[INFO] ------------------------------------------------------------------------
	[INFO] Reactor Summary:
	[INFO]
	[INFO] exitsoft parend ................................... SUCCESS [0.797s]
	[INFO] exitsoft common jar ............................... SUCCESS [55.718s]
	[INFO] exitsoft orm jar .................................. SUCCESS [5.579s]
	[INFO] vcs admin panel ................................... SUCCESS [5.734s]
	[INFO] vcs admin panel use jpa ........................... SUCCESS [0.203s]
	[INFO] exit web framework project ........................ SUCCESS [0.313s]
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 1:08.828s
	[INFO] Finished at: Fri Jan 04 11:11:00 CST 2013
	[INFO] Final Memory: 9M/22M
	[INFO] ------------------------------------------------------------------------
***
