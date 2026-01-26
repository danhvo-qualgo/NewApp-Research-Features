module Fastlane
  module Actions
    class ActBumpVersionAction < Action
      def self.run(params)
        version_name = params[:name]
        version_code = params[:code]
        auto_increment = params[:auto_increment]

        # Find project root by looking for config/version directory
        project_root = Dir.pwd
        while project_root != "/" && !File.exist?(File.join(project_root, "config", "version", "version.properties"))
          project_root = File.dirname(project_root)
        end

        version_file = File.join(project_root, "config", "version", "version.properties")

        unless File.exist?(version_file)
          UI.user_error!("version.properties file not found at #{version_file}")
        end

        # Read current version properties
        current_props = load_properties(version_file)
        current_name = current_props["VERSION_NAME"] || "1.0.0"
        current_code = current_props["VERSION_CODE"]&.to_i || 1

        UI.message("Current version: #{current_name} (#{current_code})")

        # Determine new version name
        new_name = version_name || current_name

        # Determine new version code
        if version_code
          new_code = version_code.to_i
        elsif auto_increment
          new_code = current_code + 1
        else
          new_code = current_code
        end

        # Validate version name format (flexible dotted versioning)
        unless new_name.match?(/^\d+(\.\d+)+$/)
          UI.user_error!("Version name must follow dotted format (e.g., 1.2.3)")
        end

        # Validate version code
        if new_code <= 0
          UI.user_error!("Version code must be a positive integer")
        end

        # Update version file
        new_content = "VERSION_NAME=#{new_name}\nVERSION_CODE=#{new_code}\n"
        File.write(version_file, new_content)

        UI.success("✅ Version updated: #{new_name} (#{new_code})")

        # Return updated values
        {
          version_name: new_name,
          version_code: new_code,
          previous_name: current_name,
          previous_code: current_code
        }
      end

      def self.load_properties(file)
        return {} unless File.exist?(file)
        File.readlines(file).each_with_object({}) do |line, hash|
          next if line.strip.empty? || line.strip.start_with?('#')
          key, value = line.strip.split('=', 2)
          hash[key.strip] = value.strip if key && value
        end
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :name,
            description: "Version name (e.g., 1.2.3)",
            optional: true,
            type: String
          ),
          FastlaneCore::ConfigItem.new(
            key: :code,
            description: "Version code (e.g., 42)",
            optional: true,
            type: Integer
          ),
          FastlaneCore::ConfigItem.new(
            key: :auto_increment,
            description: "Auto increment version code by 1",
            optional: true,
            default_value: true,
            type: Boolean
          )
        ]
      end

      def self.description
        "Bump app version name and/or code"
      end

      def self.authors
        ["Boss"]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end